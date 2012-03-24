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
import java.util.*;
import nbl.utilj.*;

/** Network info manager.<p>
 *
 *  Maintains a list and hts of {@link NetworkInfo} objects for all
 *  ec2 networks.  This class contains a static instance of itself,
 *  which is the sole source for {@link NetworkInfo} in the app.<p>
 *
 *  The list of {@link NetworkInfo} objects is updated by
 *  {@link #findNetworks()}, which queries aws for information.<p>
 *
 * @author Barnet Wagman
 */
public class NiM {

    private static NiM nim;

    public static void init() {
        nim = new NiM();
    }

    public static boolean register(NetworkInfo ni) {
        return( nim.registerNi(ni) );
    }

    public static void update(Ec2Wrangler ec2w) {
        update(ec2w.getInstancesAllListed());
    }

    public static void update(List<InstanceStatus> instances) {
        nim.findNetworks(instances);
    }

    public static NetworkInfo getForID(String networkID) {
        return( nim.htByID.get(networkID) );
    }

        /** Blocks until the ni is available or timeout is reached.
         *
         * @param networkID
         * @param maxWait
         * @return
         */
    public static NetworkInfo getForIDWhenAvailable(String networkID,
                                                    long maxWait)
        throws NoSuchNetworkException {
        long tm0 = System.currentTimeMillis();
        while( (System.currentTimeMillis() - tm0) <= maxWait ) {
            NetworkInfo ni = nim.htByID.get(networkID);
            if ( ni != null ) return(ni);
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }
        throw new NoSuchNetworkException("No network found with ID=" + networkID);
    }

    public static NetworkInfo getForName(String networkName) {
        return( nim.htByName.get(networkName) );
    }

    public static NetworkInfo getForMasterHostname(String hostName) {
        for (NetworkInfo ni : nim.list) {
            if ( ni.getMastersPublicDnsName().equals(hostName) )
                return(ni);
        }
        return(null);
    }

    public static List<NetworkInfo> getNetworks() {
        return( nim.list );
    }

    public static boolean IDExists(String ID) {
        if ( nim.htByID == null ) return(false);
        else return( nim.htByID.containsKey(ID) );
    }
    
    public static boolean nameExists(String name) {
        if ( nim.htByName == null ) return(false);
        else return( nim.htByName.containsKey(name) );
    }

         /** Creates a string that identifies a set ec2 instances
         *  that constitute an mpi
         *  network.  The ID is <tt>ec2-mpi-&lt;date time&gt;</tt> e.g.
         *  <blockquote><tt>
         *  ec2-mpi-20110221211549123
         *  </blockquote></tt><p>
         *
         *  Each instance is given a <tt>networkID</tt> tag with
         *  the value created by this method.
         */
    public static String createNetworkID() {

        for (;;) {
            long time = System.currentTimeMillis();
            String ID = Constants.NETWORD_ID_PREFIX +
                        Long.toString(UtilPPE.toYMDHMSM(time));
            if ( !IDExists(ID) ) return(ID);
            try { Thread.sleep(100L); } catch(InterruptedException ix) {}
        }
    }

    public static String createUniqueDefaultNetworkName() {

        String name = SysProp.user_name.getVal() + "-" +
                      Integer.toString( UtilPPE.toYMD(System.currentTimeMillis()) );

        if ( !nameExists(name) ) return(name);
        else {
            for ( int i = 0; i < Integer.MAX_VALUE; i++ ) {
                String nm = name + "_" + Integer.toString(i);
                if( !nameExists(nm) ) return(nm);
            }
        }
        throw new RuntimeException("Failed to create a unique default " +
                                   "network name.");
    }

    public static String summarize() {

        StringBuilder s = new StringBuilder();

        List<NetworkInfo> nis = getNetworks();
        s.append("N networks=" + nis.size() + "\n");

        for ( NetworkInfo ni : nis ) {
            s.append("  " + ni.toString() + "\n");
            for ( InstanceStatus ins : ni.instances ) {
                s.append("     " + ins.getPublicDnsName() + " " +
                         ins.instance.getState().getName() + " " +
                         ins.getTagValue(InstanceTag.nodeType) + "\n");
            }
        }
        return(s.toString());
    }

//    public static boolean anythingPending() {
//        for ( NetworkInfo ni : nim.list ) {
//            if ( ni.isPending() ||
//                 (!ni.isRunning() && !ni.isNil()) ||
//                 (ni.isRunning() ) ||
//                 ni.hasRequestPending() )
//                return(true);
//        }
//        return(false);
//    }

    public static void addStateChangeListener(StateChangeListener listener) {
        if ( !nim.stateChangeListeners.contains(listener) )
            nim.stateChangeListeners.add(listener);
    }

    public static void fireStateChangeEvent() {
        // for ( StateChangeListener l : nim.stateChangeListeners ) {
        // ^ The concurrent modification bug.
        for ( int i = 0; i < nim.stateChangeListeners.size(); i++ ) {
            nim.stateChangeListeners.get(i).stateChanged();
//            /* D */ System.out.println("NiM fired stateChanged on " +
//                             nim.stateChangeListeners.get(i).getClass().getName() );
        }
    }

    public static boolean networkRunning() {
        return( nim.hasRunningNetwork() );
    }

        // ------------------------------------------------


    private HashMap<String,NetworkInfo> htByName;
    private HashMap<String,NetworkInfo> htByID;
    private List<NetworkInfo> list;
    
    private List<StateChangeListener> stateChangeListeners;
  
    private NiM() {      
        stateChangeListeners = new ArrayList<StateChangeListener>();
        htByName = new HashMap<String, NetworkInfo>();
        htByID = new HashMap<String, NetworkInfo>();
        list = new ArrayList<NetworkInfo>();   
    }

        /**
         *
         * @param ni
         * @return true if the ni was register, false if it is already
         * in registered or can't be registered.
         */
    private boolean registerNi(NetworkInfo ni) {

        String ID = ni.getNetworkID();
        if ( ID == null ) return(false);

        if ( htByID.containsKey(ID) ) {
            String name = ni.getNetworkName();
            if ( (name != null) && !htByName.containsKey(name) )
                htByName.put(name, ni);
            return(false);
        }
        else {
            list.add(ni);
            htByID.put(ID, ni);
            String name = ni.getNetworkName();
            if ( name != null ) {
                htByName.put(name,ni);
            }
            return(true);
        }
    }

        /** This method updates the {@link NetworkInfo} objects
         *  based on existing instances.  {@link NetworkInfo}s that
         *  no longer match existing instances are purged.
         *
         * @param instances
         */
    private void findNetworks(List<InstanceStatus> instances) {

            // Clear instances lists in nis
        for ( NetworkInfo ni : list ) ni.clearInstances();
      
        NEXT: for ( InstanceStatus s : instances ) {
            String ID = s.getNetworkID();
            String name = s.getNetworkName();
            if ( (ID == null) || (name == null) ) {
                continue NEXT;
            }
            NetworkInfo ni = htByID.get(ID);                      
            if ( ni == null ) { // Create the ni                    
                ni = new NetworkInfo(ID,name);
                ni.setEndogeous(false);
                register(ni);
            }
                        
            ni.add(s);          
        }

        Collections.sort(list);

        for ( NetworkInfo ni : list ) ni.setState();
            // ^ This must be done AFTER the instances have be
            //   assigned to ni.
            
        fireStateChangeEvent();
    }

    boolean hasRunningNetwork() {
        if ( list.size() < 1 ) return(false);
        for ( NetworkInfo ni : list ) {
            if ( ni.isRunning() ) return(true);
        }
        return(false);
    }
}
