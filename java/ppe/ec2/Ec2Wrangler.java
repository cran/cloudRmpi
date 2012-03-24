/*
    Copyright 2011 Northbranchlogic, Inc.

    This file is part of Parallel Processing with EC2 (ppe).

    ppe is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ppe is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package ppe.ec2;

import ppe.*;
import com.amazonaws.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import java.io.*;

/** All operations that require sending requests to AWS re ec2 are implemented
 *  in this class.
 *
 * @author Barnet Wagman
 */
public class Ec2Wrangler {

    private ParamsEc2 params;

    public AmazonEC2Client ec2Client;
    private SecurityGroupFactory sgFactory;
//    Log log;
   
    public Ec2Wrangler(ParamsEc2 params) throws ParamMissingException {
        this.params = params;
        ec2Client = new AmazonEC2Client(params.buildAWSCredentials());
        this.sgFactory = new SecurityGroupFactory(params,ec2Client);
//        NiM.update(getInstancesAllListed());
//        log = LogFactory.getLog(NearNilLog.class);
    }

    public ParamsEc2 getParams() { return(params); }
    public void setParams(ParamsEc2 params) { this.params = params; }

        // --------- Create network ----------------------------

        /** Blocks until all requested ec2 instances are up and running.
          *
          * @return networkID
          * @throws IOException
          * @throws ParamMissingException
          * @throws ImproperParamException
          * @throws IncompatibleInstanceImageException
          * @throws NoSuchAmiException
          * @throws NoSuchInstanceException
          */
    public String createNetwork(NetworkSpec spec)
        throws IOException, ParamMissingException, ImproperParamException,
               IncompatibleInstanceImageException,
               NoSuchAmiException, NoSuchInstanceException,
               IncompleteNetworkSpecException,
               NoSuchNetworkException {

        if ( !spec.isComplete() )
            throw new IncompleteNetworkSpecException(
                                    spec.listMissingParams());
       
        UtilEc2.checkInstanceImageCompatibility(spec.instanceType,
                                                spec.imageID,
                                                ec2Client);     
        String networkID;
      
        if ( !spec.useSpotInstances ) networkID = launchInstances(spec);
        else {
            networkID = launchSpotInstances(spec);
//            /* D */ System.out.println("# Ec2w # back from launching spots");
        }

            // The wait should be quite long; it can take some time
            // for the network to show up and we cannot proceed
            // to start services until the network is visible.
        NetworkInfo ni = NiM.getForIDWhenAvailable(networkID,
                                    Constants.MAX_WAIT_FOR_NEW_NETWORK_INFO);
//         /* D */ System.out.println("# Ec2w # got ni for " + networkID +
//                    " master=" + ni.getMastersPublicDnsName() +
//                    " n instance expected=" + spec.nInstances);
            // Wait for all instances to be up.
        waitForInstances(ni,spec.nInstances);
//        /* D */ System.out.println("# Ec2w # done waiting for instances.");
        NiM.update(this);

        return(networkID);
    }

        // --------- Launch instances --------------------------

        /** This version of <tt>launchInstances()</tt> gets all
         *  parameters from method arguments. 
         *
         * @param instanceType
         * @param imageID
         * @param minCountInstances
         * @param maxCountInstances
         * @param keyName
         * @param securityGroupName
         * @param networkName
         * @throws ParamMissingException
         * @return networkID
         */
    public String launchInstances(InstanceType instanceType,
                                  String imageID,
                                  String availabilityZone,
                                  int minInstances,
                                  int maxInstances,
                                  String keyName,
                                  String securityGroupName,
                                  String networkName)
        throws ParamMissingException, ImproperParamException {

        if ( (minInstances < 1) || (maxInstances < minInstances) )
            throw new ImproperParamException("instances min=" +
                    minInstances + " max=" + maxInstances);
                   
        RunInstancesRequest req = new RunInstancesRequest();
        req.setInstanceType(instanceType);
        req.setImageId(imageID);
        if ( AmiDescription.isSpecifiedValidAvailabilityZone(ec2Client,
                                                             availabilityZone) )
                    setAvailabilityZone(req,availabilityZone);
        // else any zone will do, so don't set it.
        req.setMinCount(minInstances);
        req.setMaxCount(maxInstances);
        req.setKeyName(keyName);
        List<String> sgs = new ArrayList<String>();
        sgs.add(securityGroupName);
        req.setSecurityGroups(sgs);

        String networkID = NiM.createNetworkID();
        NetworkInfo ni = new NetworkInfo(networkID,networkName);
        NiM.register(ni);
        ni.setState(NetworkInfo.State.pending);
       
        if ( isHVM(imageID) ) setupClusterPlacementGroup(req);

        RunInstancesResult rr = ec2Client.runInstances(req);

        NiM.update(this); // Update the network info

        waitForAllInstancesToBeRunning(rr);
        
        tagInstances(getInstanceIDs(rr),networkID,networkName);
        
        NiM.update(getInstancesAllListed()); // Update the network info
       
        return(networkID);
    }

    private void setAvailabilityZone(RunInstancesRequest req,String zone) {
        try {
            Placement p = new Placement(zone);
            req.setPlacement(p);
        }
        catch(Exception xxx) { ExceptionHandler.display(xxx); }
    }

    private void setAvailabilityZone(LaunchSpecification spec,String zone) {
        try {
//            Placement p = new Placement(zone);
            SpotPlacement p = new SpotPlacement(zone);
            // Placement changed to SpotPlacement migrating from
            // AWS java sdk 1.1.6 -> 1.3.3
            spec.setPlacement(p);
        }
        catch(Exception xxx) { ExceptionHandler.display(xxx); }
    }

        /** If {@link NetworkSpec#securityGroupName} is null,
         *  a one time default security group is created.
         *  min and max n instances are set to
         *  {@link NetworkSpec#nInstances}.
         *
         * @param spec
         * @return network ID
         */
    public String launchInstances(NetworkSpec spec)
        throws ParamMissingException, ImproperParamException {

        if ( spec.securityGroupName == null )
            spec.securityGroupName =
                   sgFactory.createOneTimeSecurityGroup();

        return( launchInstances(spec.instanceType,
                                spec.imageID,
                                spec.availabilityZone,
                                spec.nInstances,
                                spec.nInstances,
                                spec.keyName,
                                spec.securityGroupName,
                                spec.networkName) );
    }   

        // --------- Launch spot instances ---------------------

        /** Note that spot price instances are <i>not</i> available for
         *  cluster instances.<p>
         *
         *  This method blocks until the spot order has been filled.
         *  This is necessary so that we can tag the instances.  Note
         *  when this method returns the instances may not be fully booted.
         *
         * @param instanceType
         * @param imageID
         * @param nInstances
         * @param keyName
         * @param securityGroupName
         * @param networkName,
         * @param spotPrice
         * @return network ID
         * @throws ParamMissingException
         */
    public String launchSpotInstances(InstanceType instanceType,
                                      String imageID,
                                      String availabilityZone,
                                      int nInstances,
                                      String keyName,
                                      String securityGroupName,
                                      String networkName,
                                      double spotPrice)
        throws ParamMissingException, ImproperParamException {

        LaunchSpecification spec = new LaunchSpecification();        
        spec.setInstanceType(instanceType);
        spec.setImageId(imageID);
        if ( AmiDescription.isSpecifiedValidAvailabilityZone(ec2Client, 
                                                             availabilityZone) )
            setAvailabilityZone(spec,availabilityZone);
        // else any zone will do, so don't set it.
        spec.setKeyName(keyName);
        List<String> sgs = new ArrayList<String>();
        sgs.add(securityGroupName);
        spec.setSecurityGroups(sgs);
        if ( isHVM(imageID) ) setupClusterPlacementGroup(spec);
        RequestSpotInstancesRequest reqSpot =
                        new RequestSpotInstancesRequest();
        reqSpot.setInstanceCount(new Integer(nInstances));
        reqSpot.setSpotPrice(UtilPPE.f2(spotPrice));

        reqSpot.setLaunchSpecification(spec);        

        String networkID = NiM.createNetworkID();
        NetworkInfo ni = new NetworkInfo(networkID, networkName);
        NiM.register(ni);
        ni.setState(NetworkInfo.State.spotRequestPending);

        RequestSpotInstancesResult rr = ec2Client.requestSpotInstances(reqSpot);
        
            // In order to tag the instances, we must wait until the spot
            // orders have been placed.
        HashMap<String,String> sirHt = getSpotInstanceRequestIDs(rr);

        /* D */ if ( sirHt.size() != nInstances ) {
            ExceptionHandler.gui(new RuntimeException("Spot ht.size=" +
                    sirHt.size() + " nInstances=" + nInstances));
        }

        List<String> instanceIDs;
        while ( (instanceIDs = getIIDsForSpotRequest(sirHt)).size()
                < sirHt.size() ) {
//            /* D */ System.out.println("Ec2W waiting for spots: n=" +
//                    instanceIDs.size() + "/" + sirHt.size() + "/" +
//                    nInstances);
            try { Thread.sleep(Constants.SPOT_STATE_NAP_TIME); }
            catch(InterruptedException ix) {}
        }

        ni.setState(NetworkInfo.State.pending);
//         /* D */ System.out.println("Ec2W DONE waiting for spots: n=" +
//                    instanceIDs.size() + "/" + sirHt.size() + "/" +
//                    nInstances);

        tagInstances(instanceIDs,networkID,networkName);
      
        NiM.update(getInstancesAllListed()); // Update the network info
        
        return(networkID);
    }

        /** If {@link NetworkSpec#securityGroupName} is null,
         *  a one time default security group is created.
         * 
         * @param spec
         * @return network ID
         */
    public String launchSpotInstances(NetworkSpec spec)
        throws ParamMissingException, ImproperParamException {
        if ( spec.securityGroupName == null )
            spec.securityGroupName =
                   sgFactory.createOneTimeSecurityGroup();

        return( launchSpotInstances(spec.instanceType,
                                    spec.imageID,
                                    spec.availabilityZone,
                                    spec.nInstances,
                                    spec.keyName,
                                    spec.securityGroupName,
                                    spec.networkName,
                                    spec.spotPrice) );
    }

        /** Waits until all expected instances have status 'running'.
         * 
         * @param networkName
         * @param n number of instances expected
         * @return
         */
    public void waitForInstances(NetworkInfo ni,int nInstancesExpected) {       
        while ( !allRunning(getInstances(ni),nInstancesExpected) ) {
            try { Thread.sleep(Constants.SPOT_STATE_NAP_TIME); }
            catch(InterruptedException ix) {}
        }
    }

        /** Blocks until all instances in <tt>rr</tt> have ec2 status
         *  'running'.
         * @param rr
         */
    private void waitForAllInstancesToBeRunning(RunInstancesResult rr) {

        while ( !allRunning(rr) ) {
//            /* D */ System.out.println("WRANGLER: !all running");
            try { Thread.sleep(Constants.SPOT_STATE_NAP_TIME); }
            catch(InterruptedException ix) {}
        }
    }

        // ---------- Status and dns ----------------------------------------

        /** Gets {@link InstanceStatus}s for all instances owned by the
         *  user.  This list includes instances that are running or
         *  pending, but not those that have been shutdown.
         *
         * @return
         */
    public List<InstanceStatus> getInstancesActive() {

        List<InstanceStatus> ins = new ArrayList<InstanceStatus>();

        DescribeInstancesResult r = ec2Client.describeInstances();

        for ( Reservation res : r.getReservations() ) {
            List<String> sgns = res.getGroupNames();
            for ( Instance in : res.getInstances() ) {
                InstanceStatus s = new InstanceStatus(in,sgns);
                if ( s.isActive() ) ins.add(s);
            }
        }
        return(ins);
    }

        /** Gets {@link InstanceStatus}s for all instances owned by the
         *  user, including any that are not active.
         *
         * @return
         */
    public List<InstanceStatus> getInstancesAllListed() {

        List<InstanceStatus> ins = new ArrayList<InstanceStatus>();

        DescribeInstancesResult r = ec2Client.describeInstances();

        for ( Reservation res : r.getReservations() ) {
            List<String> sgns = res.getGroupNames();
            for ( Instance in : res.getInstances() ) {
                InstanceStatus s = new InstanceStatus(in,sgns);
                ins.add(s);
            }
        }
        return(ins);
    }

        /** Returns status objects for instances listed in <tt>rir</tt>
         *  that are 'active', i.e. have ec2 status 'running' pr
         *  'pending'.
         *
         * @param rir
         * @return
         */
    public static List<InstanceStatus> getInstances(RunInstancesResult rir) {

        List<InstanceStatus> ins = new ArrayList<InstanceStatus>();

        Reservation r = rir.getReservation();

        for ( Instance in : r.getInstances() ) {
            InstanceStatus s = new InstanceStatus(in,r.getGroupNames());
            if ( s.isActive() ) ins.add(s);
        }
        return(ins);
    }

        /** Returns status objects for instances listed in <tt>rir</tt>
         *  that have ec2 status 'running'.
         *
         * @param rir
         * @return
         */
    public static List<InstanceStatus> getRunningInstances(RunInstancesResult rir) {

        List<InstanceStatus> ins = new ArrayList<InstanceStatus>();

        Reservation r = rir.getReservation();

        for ( Instance in : r.getInstances() ) {
            InstanceStatus s = new InstanceStatus(in,r.getGroupNames());
            if ( s.isActive() ) ins.add(s);
        }
        return(ins);
    }


    public static List<String> getInstanceIDs(RequestSpotInstancesResult rir) {

        List<String> IDs = new ArrayList<String>();
        for ( SpotInstanceRequest sr : rir.getSpotInstanceRequests() ) {
            IDs.add(sr.getInstanceId());
        }
        return(IDs);
    }

    public static List<String> getInstanceIDs(RunInstancesResult rir) {

        List<String> IDs = new ArrayList<String>();

        for ( Instance ins : rir.getReservation().getInstances() ) {
            IDs.add(ins.getInstanceId());
        }
        return(IDs);
    }
   
    public InstanceStatus getMaster(NetworkInfo ni) {
        List<InstanceStatus> lst = getInstances(ni);
        for ( InstanceStatus ins : lst ) {
            if ( ins.isMaster() ) return( ins );
        }
        return(null);
    }

    public List<InstanceStatus> getInstances(NetworkInfo ni) {

        List<InstanceStatus> statuses = new ArrayList<InstanceStatus>();

        DescribeInstancesResult ir = ec2Client.describeInstances();
        for (Reservation r : ir.getReservations() ) {
            List<String> sgns = r.getGroupNames();
            List<Instance> insL = r.getInstances();
            for ( Instance ins : insL ) {
                InstanceStatus x = new InstanceStatus(ins,sgns);
                if ( x.hasNetworkID(ni.ID) ) statuses.add(x);
            }
        }
        return(statuses);
    }

    private List<Instance> getAllUsersInstances() {

        List<Instance> instances = new ArrayList<Instance>();

        DescribeInstancesResult ir = ec2Client.describeInstances();
        for (Reservation r : ir.getReservations() ) {
            instances.addAll(r.getInstances());
        }
        return(instances);
    }

    public List<InstanceStatus> getSlaves(NetworkInfo ni) {

        List<InstanceStatus> lst = getInstances(ni);
        List<InstanceStatus> slaves = new ArrayList<InstanceStatus>();
        for ( InstanceStatus ins : lst ) {
            if ( ins.isSlave() ) slaves.add(ins);
        }
        return(slaves);
    }

        /** Gets the networkNames of all ec2-mpi networks owned
         *  by the user that are active.  A network is defined
         *  as 'active' if it's master ec2 instance is either
         *  "pending" or "running".
         *
         * @return
         */
    public List<String> getActiveNetworkNames() {

        List<String> networkNames = new ArrayList<String>();

        DescribeInstancesResult ir = ec2Client.describeInstances();
        for (Reservation r : ir.getReservations() ) {
            List<String> sgns = r.getGroupNames();
            List<Instance> insL = r.getInstances();
            for ( Instance ins : insL ) {
                InstanceStatus x = new InstanceStatus(ins,sgns);
                if ( x.isActive() ) {
                    if ( !networkNames.contains(x.getNetworkName()) )
                        networkNames.add(x.getNetworkName());
                }
            }
        }
        return(networkNames);
    }

    public String getMasterPublicDns(NetworkInfo ni) {       
        InstanceStatus m = getMaster(ni);
        if ( m != null ) return(m.getPublicDnsName());
        else return(null);    
    }

    public List<String> getSlavesPublicDns(NetworkInfo ni) {
        List<InstanceStatus> sL = getSlaves(ni);
        List<String> sds = new ArrayList<String>();
        for (InstanceStatus s : sL ) {
            sds.add(s.getPublicDnsName());
        }
        return(sds);
    }

    public List<String> getPublicDns(NetworkInfo ni) {
        List<InstanceStatus> sL = getInstances(ni);
        List<String> sds = new ArrayList<String>();
        for (InstanceStatus s : sL ) {
            sds.add(s.getPublicDnsName());
        }
        return(sds);
    }

        // --------- Terminate instances -------------------------

        /** Terminates the instances with the specified instance IDs.
         *
         * @param instanceIDs
         * @return
         */
    public TerminateInstancesResult terminateInstances(List<String> instanceIDs) {
        TerminateInstancesRequest term = new TerminateInstancesRequest(instanceIDs);
        TerminateInstancesResult r = ec2Client.terminateInstances(term);
        deleteClusterGroupIfNecessary();
        return(r);
    }

        /** Terminates the instances in the specified network and
         *  deletes the one time security group that they were members of.
         *
         * @param networkID
         * @return null if no instances with networkID were found.
         */
    public TerminateInstancesResult terminateInstances(NetworkInfo ni) {

        List<String> iids = getInstanceIDsFor(ni);
        if ( iids.size() < 1 ) return(null);

        List<String> securityGroups =
            sgFactory.getOneTimeSecurityGroups(ni);

        TerminateInstancesResult r = terminateInstances(iids);

        for ( String sg : securityGroups ) {
            sgFactory.deleteOneTimeSecurityGroup(sg);
        }

        return(r);
    }

    public List<String> getKeypairNames() {
        List<String> names = new ArrayList<String>();
        DescribeKeyPairsResult r = ec2Client.describeKeyPairs();
        for ( KeyPairInfo kpi : r.getKeyPairs() ) {
            String nm = kpi.getKeyName();
            if ( nm != null ) names.add(nm);
        }
        return(names);
    }

        // ------------- Reboot -------------------------

    /** Blocks with {@link NetworkInfo#pingAllInstances() }
     *  until all networks are available (or ping times out).
     * 
     * @param ni
     * @throws Exception 
     */    
    public void rebootInstances(NetworkInfo ni) throws Exception {
        ni.setState(NetworkInfo.State.rebooting);
        List<String> iids = getInstanceIDsFor(ni);
        if ( iids.size() < 1 ) return;
        rebootInstances(iids);
            // Wait for the reboots to take effect before we start
            // pinging to determine when the reboot is done.
        try { Thread.sleep(20L * 1000L); }
        catch(InterruptedException ix) {}

        ni.pingAllInstances();            

            // Set the state to non-rebooting to the 'true' state 
            // can be discovered.
        ni.setState(NetworkInfo.State.nil);

            // Reset the state endogenously
        ni.setState();
    }

    public void rebootInstances(List<String> instanceIDs) {
        RebootInstancesRequest rir = new RebootInstancesRequest(instanceIDs);
        ec2Client.rebootInstances(rir);
    }

    public static String chooseNetworkName(ParamsEc2 params) {

        if ( (params.networkName != null) &&
             !NiM.nameExists(params.networkName) )
            return(params.networkName);
        else return(NiM.createUniqueDefaultNetworkName());
    }

    
        // --------------------------------------------------------
    
        /** Every instance gets three tags attached to it:
         *  <ul>
         *  <li>networkID</li>
         *  <li>networtName</li>
         *  <li>nodeType</li>
         *  </ul>
         *  This method chooses the master.
         * @param rr
         */
    private void tagInstances(List<String> instanceIDs,
                              String networkID,String networkName) {

        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(InstanceTag.networkID.toString(),networkID));
        tags.add(new Tag(InstanceTag.networkName.toString(),networkName));

            // Assign the common tags
        CreateTagsRequest req = new CreateTagsRequest(instanceIDs,tags);
        ec2Client.createTags(req);

            // Now specify the master and slaves.
        String master = instanceIDs.get(0);
        List<String> slaves = new ArrayList<String>();
        for ( int i = 1; i < instanceIDs.size(); i++ ) {
            slaves.add(instanceIDs.get(i));
        }

        List<Tag> masterTag = new ArrayList<Tag>();
        masterTag.add(new Tag(InstanceTag.nodeType.toString(),
                              NodeType.master.toString()));
        List<String> masterR = new ArrayList<String>();
        masterR.add(master);
        req = new CreateTagsRequest(masterR,masterTag);
        ec2Client.createTags(req);

        if ( slaves.size() > 0 ) {
            List<Tag> slaveTag = new ArrayList<Tag>();
            slaveTag.add(new Tag(InstanceTag.nodeType.toString(),
                                 NodeType.slave.toString()));
            req = new CreateTagsRequest(slaves,slaveTag);
            ec2Client.createTags(req);
        }
    }

        /** Used by <i>other</i> classes to change an instance's tags.
         * 
         * @param instanceID
         */
    public void setTags(String instanceID,
                        String networkID, String networkName,
                        NodeType nodeType) {

        List<Tag> tags = new ArrayList<Tag>();

        tags.add(new Tag(InstanceTag.networkID.toString(),networkID));
        tags.add(new Tag(InstanceTag.networkName.toString(),networkName));
        tags.add(new Tag(InstanceTag.nodeType.toString(),
                 nodeType.toString()));

        List<String> instanceIDs = new ArrayList<String>();
        instanceIDs.add(instanceID);

        CreateTagsRequest req = new CreateTagsRequest(instanceIDs,tags);
        ec2Client.createTags(req);
    }

    private void throwMissingParamsEx(String name)
        throws ParamMissingException {
        System.err.println("Param " + name + " is " +
                            params.getMissingParamMessage() +
                            "\nParams:\n" + params.toString() + "\n");
        Thread.dumpStack();
     
        throw new ParamMissingException("Param " + name + " is " +
                                        params.getMissingParamMessage());
    }

        /** Returns the network IDs of active instances with the
         *  network name.
         * 
         * @param networkName
         * @return
         */
    private List<String> getNetworkIDsForNetworkName(String networkName) {

        DescribeInstancesResult ir = ec2Client.describeInstances();
        List<String> IDs = new ArrayList<String>();

        for ( Reservation r : ir.getReservations() ) {
            List<String> sgns = r.getGroupNames();
            List<Instance> insL = r.getInstances();
            for ( Instance ins : insL ) {
                InstanceStatus x = new InstanceStatus(ins,sgns);
                if ( x.isActive() ) {
                    String nwi = x.getNetworkName();
                    if ( (nwi != null) && nwi.equals(networkName) )
                        IDs.add( x.getNetworkID() );
                }
            }
        }
        return(IDs);
    }

    private String getCurrentNetworkID(List<String> networkIDs) {

        if ( (networkIDs == null) || (networkIDs.size() < 1) )
            return(null);

        String curNetID = null;
        long curTm = -1000L;

        for ( String netID : networkIDs ) {
            long cur = getLongTime(netID);
            if ( cur > curTm ) {
                curNetID = netID;
                curTm = cur;
            }
        }

        return(curNetID);
    }

    private String getCurrentNetworkIDForNetworkName(String networkName) {
        return(getCurrentNetworkID(getNetworkIDsForNetworkName(networkName)));
    }

    private long getLongTime(String networkID) {

        if ( networkID.startsWith(Constants.NETWORD_ID_PREFIX) ) {
            String ts = networkID.substring(Constants.NETWORD_ID_PREFIX.length());
            return( Long.parseLong(ts) );
        }
        else throw new RuntimeException("BAD networkID=" + networkID);
    }

    private List<String> getInstanceIDsFor(NetworkInfo ni) {
            // String networkID) {

        List<String> iids = new ArrayList<String>();

        List<InstanceStatus> ists = getInstances(ni);
//                getStatusForNetworkID(networkID);

        for ( InstanceStatus s : ists ) {
            iids.add(s.instance.getInstanceId());
        }
        return(iids);
    }

    private HashMap<String,String> getSpotInstanceRequestIDs(RequestSpotInstancesResult rr) {

        HashMap<String,String> ht = new HashMap<String,String>();
        for ( SpotInstanceRequest s : rr.getSpotInstanceRequests() ) {
            String ID = s.getSpotInstanceRequestId();
            ht.put(ID,ID);
        }
        return(ht);
    }

    private List<String> getIIDsForSpotRequest(HashMap<String,String> sirHt) {

        try {
            List<String> iids = new ArrayList();

            DescribeSpotInstanceRequestsResult drr =
                    ec2Client.describeSpotInstanceRequests();

            List<SpotInstanceRequest> sirs = drr.getSpotInstanceRequests();

            for ( SpotInstanceRequest r : sirs ) {
                if ( sirHt.containsKey(r.getSpotInstanceRequestId()) ) {
                   String ID = r.getInstanceId();
                   if ( ID != null ) iids.add(ID);
                }
            }
            return(iids);
        }
        catch(AmazonClientException acx) {
            System.err.println("Exception in Ec2Wrangler.getSpotInstanceRequestIIDs(): " +
                    acx.getMessage() + "  - we'll try again.");
            return(new ArrayList());
        }        
    }

        /** Tests whether all listed instances have ec2 status 'running'.
         *
         * @param insL
         * @param nInstancesExpected
         * @return
         */
    private boolean allRunning(List<InstanceStatus> insL,int nInstancesExpected) {

        if ( insL.size() < nInstancesExpected ) return(false);
//            throw new RuntimeException("nInstancesExpected=" + nInstancesExpected +
//                        " BUT n InstanceStatus=" + insL.size());

        int n = 0;
        for ( InstanceStatus s : insL ) {
            if ( s.isRunning() ) ++n;
        }
    
        return(n >= nInstancesExpected);
    }

        /** Tests whether all instances in the reservation have
         *  ec2 status 'running'.
         * @param rr
         * @return
         */
    private boolean allRunning(RunInstancesResult rr) {
        try {
            List<Instance> extantInstances = getAllUsersInstances();

            DescribeInstancesResult ir = ec2Client.describeInstances();

            for (Instance ins : rr.getReservation().getInstances() ) {               
                Instance x = getMatchingInstance(extantInstances,ins);
                if ( x == null ) {                    
                    return(false);
                }
                else if (!InstanceStatus.isRunning(x)) {                    
                    return(false);
                }             
            }
            return(true);
        }
        catch(Exception xxx) {
            ExceptionHandler.display(xxx);
            return(false);
        }
    }

    private Instance getMatchingInstance(List<Instance> list, Instance ins ) {
        for ( Instance x : list ) {
            if ( x.getInstanceId().equals(ins.getInstanceId()) ) return(x);
        }
        return(null);
    }

    public boolean isHVM(String imageID) {

        DescribeImagesRequest req = new DescribeImagesRequest();
        List<String> iids = new ArrayList<String>();
        iids.add(imageID);
        req.setImageIds(iids);

        DescribeImagesResult res = ec2Client.describeImages(req);

        for ( Image img : res.getImages() ) {
            if ( img.getImageId().equals(imageID) ) {
                return( img.getVirtualizationType().equals("hvm") );
            }
        }
        return(false);
    }

        /** Specifies a placemen group in the <tt>req</tt>.
         *  If a the param cluster
         * @param req
         */
    private void setupClusterPlacementGroup(RunInstancesRequest req) {

        String clusterName;

        if ( params.clusterGroupName != null )
            clusterName = params.clusterGroupName;
        else clusterName = Constants.CLUSTER_GROUP_DEFAULT;

        createClusterGroupIfNecessary(clusterName);

        Placement placement = new Placement();
        placement.setGroupName(clusterName);

        req.setPlacement(placement);
    }

    private void setupClusterPlacementGroup(LaunchSpecification spec) {

        String clusterName;

        if ( params.clusterGroupName != null )
            clusterName = params.clusterGroupName;
        else clusterName = Constants.CLUSTER_GROUP_DEFAULT;

        createClusterGroupIfNecessary(clusterName);
//        Placement placement = new Placement();
        SpotPlacement placement = new SpotPlacement();
        // Placement changed to SpotPlacement migrating from
            // AWS java sdk 1.1.6 -> 1.3.3
        placement.setGroupName(clusterName);

        spec.setPlacement(placement);

//        /* D */ System.out.println("Ec2W: setup placement group name=" +
//                                clusterName + " for spot");
    }


    private void createClusterGroupIfNecessary(String name) {

        DescribePlacementGroupsResult r = ec2Client.describePlacementGroups();

        for ( PlacementGroup pg : r.getPlacementGroups() ) {
            if ( pg.getGroupName().equals(name) ) {
                /* D */ System.out.println("Ec2W: found extant placement " +
                            " group=" + name);
                return;
            }
        }
            // We need to create it.
        CreatePlacementGroupRequest req = new CreatePlacementGroupRequest();
        req.setGroupName(name);
        req.setStrategy("cluster");

        ec2Client.createPlacementGroup(req);
        /* D */ System.out.println("Ec2W: create placement group name=" +
                        name);
    }

        /** If the cluster group was not specified by the user, then
         *  the group with the default name is deleted.
         */
    private void deleteClusterGroupIfNecessary() {

        try {
            if ( params.clusterGroupName != null ) return;

            if ( placementGroupExists(Constants.CLUSTER_GROUP_DEFAULT) ) {
                DeletePlacementGroupRequest req =
                        new DeletePlacementGroupRequest(Constants.CLUSTER_GROUP_DEFAULT);
                ec2Client.deletePlacementGroup(req);
            }
        }
        catch(Exception xxx) { ExceptionHandler.display(xxx); }
    }

    private boolean placementGroupExists(String name) {

        DescribePlacementGroupsResult r = ec2Client.describePlacementGroups();

        for (PlacementGroup g : r.getPlacementGroups() ) {
            if ( g.getGroupName().equals(g) ) return(true);
        }
        return(false);
    }
}
