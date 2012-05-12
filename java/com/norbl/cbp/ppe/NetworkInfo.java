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

package com.norbl.cbp.ppe;

import com.norbl.cbp.ppe.*;
import java.util.*;
import com.norbl.util.ssh.*;


/** Holds information that identifies an ppe network.  Each network
 *  has a network ID and name, both of which are unique within a user's
 *  ec2 space.  The name and ID are attached to instances as aws tags.<p>
 *
 *  Each NetworkInfo holds a an instance of {@link Services} which is
 *  used to monitor and control services run on the network.
 *
 * @author Barnet Wagman
 */
public class NetworkInfo implements Comparable {

        /** Instance states per aws <tt>InstanceState.getState().getName()</tt>.
         *  Note that these are the values we've observed - they may not
         *  exactly match the aws documentation.<p>
         *  
         * <p>There is one exception: The aws instance object does not
         *  indicate when an instance is rebooting.  When a network is
         *  rebooting, we must explicitly set the state to 'rebooting'.
         *  The code that sets the state to 'rebooting' is responsible
         *  for calling {@link NetworkInfo#setState() } to reset the
         *  network state endogenously after rebooting (and any service
         *  restart) is finished.  The mechanism used in 
         *  cannot detect this since aws maintains the state 'running'
         *  for instance in the process of rebooting.
         * 
         */
    public enum State {
        nil("-",""),
        spotRequestPending("spot request pending",""),
        pending("instances pending","pending"),
        running("running","running"),
        servicesPending("services pending",""),
        servicesNotRunning("services not running",""),
        shuttingDown("shutting down","shutting down"),
        terminated("terminated","terminated"),
        rebooting("rebooting","rebooting");

        String title;
        String awsStateName;
        State(String title,String awsStateName) {
            this.title = title;
            this.awsStateName = awsStateName;
        }

        static State getState(String awsInstanceState) {
            for ( State s : State.values() ) {
                if ( s.awsStateName.equals(awsInstanceState) ) return(s);
            }
            return(null);
        }

        public String getTitle() { return(title); }
    }

    private Services services;

        /** Describes the overall state of all services.
         */
    private enum ServiceState { running, pending, nil };
    
    String name;
    String ID;

    public List<InstanceStatus> instances;

    private State state;
    
    /** true indicates that the network was created by this
     *  instance of the app.    
     */
    private boolean endogenousNetwork;

    public NetworkInfo(String ID,String name) {

        this.name = name;
        this.ID = ID;        
        this.instances = new ArrayList<InstanceStatus>();      
        state = state.nil;
        services = Services.createServices(ID);
        endogenousNetwork = true;
    }
    
    public void setEndogeous(boolean isEndog) { 
        this.endogenousNetwork = isEndog;
    }    

    public void setState(State state) {
        this.state = state;
    }

    public void setState() {
        
        if ( State.rebooting.equals(state) )
            return;  // When rebooting is done, the state will be
                     // explicitly set to 'running' by
                     // Ec2Wrangler.rebootInstances(NetworkInfo ni)
        
        if ( endogenousNetwork ) {
            if ( instances.size() < 1 ) return;
               // ^ The state cannot be determined endogenously
            state = findStateFromInstances();
            if ( !State.running.equals(state) ) return;

            else { // Instances running; check the services state.
                if ( services.isRunning() ) return;
                else if ( services.isPending() )
                    state = State.servicesPending;
                else if ( services.notRunning() )
                    state = State.servicesNotRunning;
            }
        }
        else {
                // The app was not created by this instance of the app
                // (it was 'discovered' by NiM.findNetworks()).
            state = findStateFromInstances();             
        }
    }

    public Services getServices() { return(services); }

        // -------------- State methods -------------------------

    public boolean isPending() { return(State.pending.equals(state)); }
    public boolean isRunning() { return(State.running.equals(state)); }
    public boolean isShuttingDown() { return(State.shuttingDown.equals(state)); }
    public boolean isTerminated() { return(State.terminated.equals(state)); }
    public boolean isShuttingDownOrTerminated() {
        return(isShuttingDown() || isTerminated());
    }
    public boolean isNil() { return(State.nil.equals(state)); }
    public boolean isServicesNotRunning() { 
        return(State.servicesNotRunning.equals(state));
    }
    

    public void setServices(Services services) {
        this.services = services;
    }

       /** Determines the state of the network based on the states of its instances.
         *
         */
    private State findStateFromInstances() {

            // Count states.
        int nNil = 0;
        int nPending = 0;
        int nShuttingDown = 0;
        int nRunning = 0;
        int nTerminated = 0;
        for ( InstanceStatus ins : instances ) {
            State s = State.getState(ins.instance.getState().getName());
            if ( s == null ) ++nNil;
            else if (State.pending.equals(s)) ++nPending;
            else if (State.shuttingDown.equals(s)) ++nShuttingDown;
            else if (State.running.equals(s)) ++nRunning;
            else if (State.terminated.equals(s)) ++nTerminated;
        }

        if ( nPending > 0 ) return(State.pending);
        else if ( nShuttingDown > 0 ) return(State.shuttingDown);
        else if ( nRunning >= instances.size() ) return(State.running);
        else if ( nTerminated >= instances.size() ) return(State.terminated);
        else if ( nTerminated > 0 ) return(State.shuttingDown);
        else return(State.nil);
    }

    /** A description of the network's state.  If the networks
     *  instances are running, this method checks the state of its
     *  services.  If services are pending, not running, or in the
     *  'nil' state, the state of the services is returned.  Otherwise
     *  the state of the network is returned.  This guarantees that
     *  'running' is only returned of the instances and services are
     *  up and running.
     * 
     * @return 
     */
    public String getStateDescription() {
        if ( endogenousNetwork ) {
            if ( !State.running.equals(state) ) return(state.getTitle());
            else if (State.servicesPending.equals(state) ||
                     State.servicesNotRunning.equals(state) )
                return(services.getStateTitle());
            else if ( services.inNilState() ) return("pending");
            else return( state.getTitle() );
        }
        else {
            return(state.getTitle());
        }
    }    

        // --------- Instance methods ---------------------------

    public void add(InstanceStatus s) {
        if ( !instances.contains(s) ) instances.add(s);
    }

    public void clearInstances() {
        instances.clear();
    }

        // -------- Getters, etc. ---------------------------------

    public String getNetworkName() { return(name); }
    public String getNetworkID() { return(ID); }

    public int getNActiveInstances() {
        int n = 0;
        for ( InstanceStatus ins : instances ) {
            if ( ins.isActive() ) ++n;
        }
        return(n);
    }

    public String getMastersPublicDnsName() {
        for ( InstanceStatus instance : instances ) {
            if ( instance.isMaster() ) return(instance.getPublicDnsName());
        }
        return(null);
    }

        /** Sort by name.
         *
         * @param other
         * @return
         */
    public int compareTo(Object other) {
        return( this.name.compareTo( ((NetworkInfo) other).name ) );
    }

    public String toString() {
        return( name + " " + ID + " n instances=" + instances.size() +
                " " + getStateDescription() );
    }

        /** Gets the earliest instance launch time.
         *
         * @return
         */
    public long getLaunchTime() {
        long lt = Long.MAX_VALUE;
        for ( InstanceStatus s : instances ) {
            Date d = s.instance.getLaunchTime();
            long tm = d.getTime();
            if ( tm < lt ) lt = tm;
        }
        return(lt);
    }    
    
    /** Blocks until all instances in the network respond to 
     *  {@link Ssh#waitForPingsFromAllHosts(java.lang.String, long)}.
     *  The max wait time for each instance is 4 minutes.
     * 
     * @throws NoSuchNetworkException
     * @throws SshPingFailureException 
     */
    public void pingAllInstances() 
         throws NoSuchNetworkException, SshPingFailureException {
        
         Pinger.waitForPingsFromAllHosts(getNetworkID(),
                                      1000L * 4L * 60L);               
    }     
}
