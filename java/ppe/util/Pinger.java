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

package ppe.util;

import ppe.ec2.*;
import java.util.*;
import utilssh.*;

/** <tt>Pinger</tt> uses {@link Ssh#pingSsh(java.lang.String) }
 *  to ping a list of instances.  It assembles two lists of instances:
 *  one of instances that were successfully pinged and one of
 *  any that were unreachable after a max time.
 *
 * @author Barnet Wagman
 */
public class Pinger {

    public List<InstanceStatus> successes;
    public List<InstanceStatus> failures;

    long maxWait;

    int n;

    public Pinger(long maxWait) {
        this.maxWait = maxWait; 
    }

    public void pingNetwork(List<InstanceStatus> instances) {

        n = instances.size();

        List<PingInstance> pingers = new ArrayList<PingInstance>();

        for ( InstanceStatus ins : instances ) {
            PingInstance pi = new PingInstance(ins);
            pingers.add(pi);
            (new Thread(pi)).start();
        }

        while ( !allDone(pingers) ) {
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }

        successes = new ArrayList<InstanceStatus>();
        failures = new ArrayList<InstanceStatus>();

        for ( PingInstance pi : pingers ) {
            if ( pi.successful ) successes.add(pi.ins);
            else failures.add(pi.ins);
        }
    }

    public boolean allSucceeded() {
        return( successes.size() == n );
    }

    public String failureNamesToHtmlLines() {

        if ( (failures == null) || (failures.size() < 1) )
            return(" ");

        StringBuilder s = new StringBuilder();
        for ( InstanceStatus f : failures ) {
            s.append(f.getPublicDnsName() + "<br>");
        }

        return( s.toString() );
    }

    private boolean allDone(List<PingInstance> pingers) {

        for ( PingInstance pi : pingers ) {
            if ( !pi.done ) return(false);
        }
        return(true);
    }

    class PingInstance implements Runnable {

        InstanceStatus ins;
        String hostName;
        boolean done;
        boolean successful;

        PingInstance(InstanceStatus ins) {
            this.ins = ins;
            this.hostName = ins.getPublicDnsName();
            successful = false;
            done = false;
        }

        public void run() {
            long tm0 = System.currentTimeMillis();
            while ( (System.currentTimeMillis() - tm0) <= maxWait) {
                if ( Ssh.pingSsh(hostName) ) {
                    successful = true;
                    done = true;
                    return;
                }
                else try { Thread.sleep(1000L); }
                     catch(InterruptedException ix) {}
            }
            done = true;
            successful = false;
        }
    }
    
    public static Pinger pingNetwork(String networkID, long maxWait)
        throws NoSuchNetworkException {

        NetworkInfo ni = NiM.getForID(networkID);
        if ( ni == null ) throw new NoSuchNetworkException("Network ID=" +
                                    networkID + " was not found.");

        List<InstanceStatus> instances = ni.instances;

        Pinger pinger = new Pinger(maxWait);
        pinger.pingNetwork(instances);

        return(pinger);
    }

    public static void waitForPingsFromAllHosts(String networkID,long maxWaitMillis)
        throws NoSuchNetworkException, SshPingFailureException {

        NetworkInfo ni = NiM.getForID(networkID);
        if ( ni == null ) throw new NoSuchNetworkException("Network ID=" +
                                    networkID + " was not found.");

        List<InstanceStatus> instances = ni.instances;
        for ( int i = 0; i < instances.size(); i++ ) {
            waitForPingSsh(instances.get(i).getPublicDnsName(),maxWaitMillis);
        }
    }
    
        /** Blocks until {@link #pingSsh(java.lang.String) } }
         *  is successful or we time out.
         * @param host
         */
    public static void waitForPingSsh(String host, long maxWaitMillis)
       throws SshPingFailureException {
        long t0 = System.currentTimeMillis();
        while ( (System.currentTimeMillis() - t0) <= maxWaitMillis ) {
            if ( Ssh.pingSsh(host) ) return;
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }
        throw new SshPingFailureException("Util.waitForPingSsh() timed out attempting to " +
            " reach " + host);
    }
}
