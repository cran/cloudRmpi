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
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import nbl.utilj.*;

/**
 *
 * @author Barnet Wagman
 */
public class SecurityGroupFactory {

    AmazonEC2Client ec2Client;
    ParamsEc2 params;

    public SecurityGroupFactory(ParamsEc2 params,AmazonEC2Client ec2Client) {
        this.params = params;
        this.ec2Client= ec2Client;
    }

        /** Creates an ec2 security group that works for mpi.  This group
         *  is created with a unique name based on <tt>user.name</tt> (unless
         *  a <tt>securityGroupNameBase</tt> has been specified as a
         *  parameter).  All ec2 instances are assigned to this security group.
         *  The security group is deleted by {@link #terminateInstances() }.
         *  If you terminate you ec2 instances any other way (e.g. using the
         *  <a href=http://aws.amazon.com/console/>AWS Management Console</a>),
         *  you should delete this security group.
         *
         * @return the new security group name.
         * @throws ParamMissingException
         */
    public String createOneTimeSecurityGroup() throws ParamMissingException {

        return(createStandardSecurityGroup(createUniqueSecurityGroupName(),
                                           Constants.SECURITY_GROUP_DES));
    }

        /** Creates a group with ssh (tcp port 22) open to the world and
         *  everything else open to group members only.
         * @param groupName
         * @return
         * @throws ParamMissingException
         */
    public String createStandardSecurityGroup(String groupName,
                                              String description)
        throws ParamMissingException {

        CreateSecurityGroupRequest req =
                new CreateSecurityGroupRequest(groupName,description);
        ec2Client.createSecurityGroup(req);

            // Now set the ips, etc.
        AuthorizeSecurityGroupIngressRequest aReq =
                new AuthorizeSecurityGroupIngressRequest();

        aReq.setGroupName(groupName);
        aReq.setIpPermissions(createMpiIpPermissions(groupName));

        ec2Client.authorizeSecurityGroupIngress(aReq);

        return(groupName);

    }

        /** Gets the name of a security group that has prefix
         *  "&lt;User name&gt;_mpi_ec2_",
         *  (where &lt;User name&gt is the value of the system property
         *  user.name), if one exists.<p>
         *
         *  If more than one security group meets the criteria, the
         *  first one encountered is return.
         *
         * @return A security group name or <tt>null</tt>.
         * @throws ParamMissingException
         */
    public String getOneTimeSecurityGroupName() throws ParamMissingException {

        DescribeSecurityGroupsResult sgr =  ec2Client.describeSecurityGroups();
        for ( SecurityGroup sg : sgr.getSecurityGroups() ) {
            if ( sg.getGroupName().startsWith(buildSecurityGroupNamePrefix()) )
                return(sg.getGroupName());
        }
        return(null);
    }

        /** This method deletes <i>all</i> security groups owned by the
         *  user that have names starting with "&lt;User name&gt;_mpi_ec2_",
         *  where &lt;User name&gt is the value of the system property
         *  user.name.
         *
         * @throws ParamMissingException
         */
    public int deleteOneTimeSecurityGroups() throws ParamMissingException {

        DescribeSecurityGroupsResult sgr =  ec2Client.describeSecurityGroups();

        int nDeleted = 0;

        String prefix = buildSecurityGroupNamePrefix();
        for ( SecurityGroup sg : sgr.getSecurityGroups() ) {
            if ( sg.getGroupName().startsWith(prefix) ) {
                DeleteSecurityGroupRequest dr =
                    new DeleteSecurityGroupRequest(sg.getGroupName());
                ec2Client.deleteSecurityGroup(dr);
                ++nDeleted;
            }
        }
        return(nDeleted);
    }

    public List<SecurityGroup> getExtantSecurityGroups() {
        return(ec2Client.describeSecurityGroups().getSecurityGroups());
    }

        /** If the specified group is not a 'one time' group, this
         *  method does nothing.  A 'one time' group has a name that
         *  starts with prefix created by {@link #buildSecurityGroupNamePrefix()}.
         *
         * @param securityGroupName
         */
    public void deleteOneTimeSecurityGroup(String securityGroupName) {

        if ( !securityGroupName.startsWith(buildSecurityGroupNamePrefix()) )
            return;

        DeleteSecurityGroupRequest dr =
                new DeleteSecurityGroupRequest(securityGroupName);
        ec2Client.deleteSecurityGroup(dr);
    }

        /** Gets a unique list of all security groups to which instances with
         *  the networkID belong.
         *
         * @param networkID
         * @return
         */
    public List<String> getSecurityGroups(NetworkInfo ni) {

        HashMap<String,String> sgHt = new HashMap<String, String>();

        DescribeInstancesResult ir = ec2Client.describeInstances();
        NEXT: for (Reservation r : ir.getReservations() ) {
            List<String> sgs = r.getGroupNames();
            List<Instance> insL = r.getInstances();
            for ( Instance ins : insL ) {
                InstanceStatus x = new InstanceStatus(ins,sgs);
                if ( x.hasNetworkID(ni.ID) ) {
                    for ( String sg : sgs ) sgHt.put(sg,sg);
                    continue NEXT;
                }
            }
        }

        List<String> usgs = new ArrayList<String>();
        for ( Iterator<String> it = sgHt.keySet().iterator(); it.hasNext(); ) {
            usgs.add(it.next());
        }
        return(usgs);
    }

    public List<String> getOneTimeSecurityGroups(NetworkInfo ni) {

        String prefix = buildSecurityGroupNamePrefix();

        List<String> sgs = getSecurityGroups(ni);
        List<String> ots = new ArrayList<String>();
        for ( String s : sgs ) {
            if ( s.startsWith(prefix) ) ots.add(s);
        }
        return(ots);
    }

        // ----------------------------------------------

    private String buildSecurityGroupNamePrefix() {
        return(SysProp.user_name.getVal() +"_mpi_ec2_");
    }

    private String createUniqueSecurityGroupName() {

        HashMap<String,SecurityGroup> ht = getExtantSecurityGroupHt();

        for ( int i = 0; i < Integer.MAX_VALUE; i++ ) {
            String nm = buildSecurityGroupNamePrefix() + Integer.toString(i);
            if ( !ht.containsKey(nm) ) return(nm);
        }
        throw new RuntimeException("Failed to create a unique security " +
                "group name with prefix " + buildSecurityGroupNamePrefix());
    }

        /**
         *
         * @return HashMap of security groups keyed by group name.
         */
    private HashMap<String,SecurityGroup> getExtantSecurityGroupHt() {
        DescribeSecurityGroupsResult r =  ec2Client.describeSecurityGroups();
        HashMap<String,SecurityGroup> ht = new HashMap<String,SecurityGroup>();
        for ( SecurityGroup sg : r.getSecurityGroups() ) {
            ht.put(sg.getGroupName(),sg);
        }
        return(ht);
    }

        /** Creates a set of permissions that will work for mpi.
         *  At present, these are hardcoded.  We may eventually need
         *  a more flexible system
         *
         * @return
         */
    private List<IpPermission> createMpiIpPermissions(String securityGroupName) {

        List<IpPermission> ipps = new ArrayList<IpPermission>();

            // Open port 22 to everyone (for ssh).
        ipps.add(createIPPermissionIP("tcp",22,22,"0.0.0.0/0"));

           // Open icmp to security group only.
        ipps.add(createIPPermissionGroup("icmp",-1,-1,securityGroupName));

            // Open all the ports above 22 to ec2 instances in this
            // group and no one else.
        ipps.add(createIPPermissionGroup("tcp",23,65535,securityGroupName));

            // Open all ports below 22 to ec2 instances in this group
            // and no one else.  This is probably unnecessary because
            // open mpi (probably) does not use them.
        ipps.add(createIPPermissionGroup("tcp",1,21,securityGroupName));
        ipps.add(createIPPermissionGroup("udp",1,65535,securityGroupName));

        return(ipps);
    }

    private IpPermission createIPPermissionIP(String protocol,
                                              int fromPort, int toPort,
                                              String ip) {
        IpPermission ipp = new IpPermission();
        ipp.setIpProtocol(protocol);
        ipp.setFromPort(fromPort);
        ipp.setToPort(toPort);
        List<String> ips = new ArrayList<String>();
        ips.add(ip);
        ipp.setIpRanges(ips);
        return(ipp);
    }

    private IpPermission createIPPermissionGroup(String protocol,
                                                 int fromPort, int toPort,
                                                 String group) {
        IpPermission ipp = new IpPermission();
        ipp.setIpProtocol(protocol);
        ipp.setFromPort(fromPort);
        ipp.setToPort(toPort);

        List<UserIdGroupPair> gps = new ArrayList<UserIdGroupPair>();
        UserIdGroupPair gp = new UserIdGroupPair();
        gp.setGroupName(group);
        gp.setUserId(params.getAWSUserID());
        gps.add(gp);

        ipp.setUserIdGroupPairs(gps);

        return(ipp);
    }

        // --------------------------------------

//    public static void createSecurityGroup(String groupName, String description)
//        throws InaccessibleFileException, java.io.IOException,
//               ParamMissingException, ImproperParamException {
//
//        ParamsEc2 params = new ParamsEc2(new String[] {});
//        params.checkForNetworkInfoParams();
//
//        AmazonEC2Client ec2Client = new AmazonEC2Client(params.buildAWSCredentials());
//        SecurityGroupFactory sgFactory =
//                new SecurityGroupFactory(params,ec2Client);
//
//        sgFactory.createStandardSecurityGroup(groupName,description);
//
//    }
//
//    public static void main(String[] argv) throws Exception {
//        createSecurityGroup("ssh-only",
//                            "ssh (tcp port 22) open to all; " +
//                            "everything else is open to group members only."
//                );
//    }
}
