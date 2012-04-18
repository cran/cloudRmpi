/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of utilssh.

    utilssh is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    utilssh is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.norbl.util.ssh;

import ch.ethz.ssh2.*;
import java.io.*;
import java.util.*;

/** Creates an ssh connection using <tt>Ganymed</tt>.
 *
 * @author Barnet Wagman
 */
public class Ssh {

    public static int N_MAX_RETRIES = 60 * 5;
    public static long RETRY_INTERVAL = 1000L;

        /** Creates an authenticated connection to ssh, retrying at intervals
         *  if necessary.
         *
         * @param hostname
         * @param user
         * @param pemFile
         * @param maxWait
         * @return
         * @throws ConnectFailureException if max wait is reached without a
         *  successful connections and authorization.
         */
    public static Connection connect(String hostname, String user,File pemFile,
                                     long maxWait)
        throws ConnectFailureException {
        
        ConnectFailureException cfx = null;
        IOException iox = null;
        long t0 = System.currentTimeMillis();
        while ( (System.currentTimeMillis() - t0) <= maxWait) {
            try {
                return(conAuth(hostname,user,pemFile));
            }
            catch(ConnectFailureException cx) {
                cfx = cx;
//                System.err.println("Util.sshConnect(): ssh connection exception:\n" +
//                            cfx.getMessage() + "\nWill retry after nap " +
//                            "host=" + hostname +
//                            "   pemFile=" + pemFile.getPath());
            }
            catch(IOException ix) { iox = ix; }

            try { Thread.sleep(RETRY_INTERVAL); } catch(InterruptedException ix) {}
        }
        String m = "";
        StackTraceElement[] stackTrace = new StackTraceElement [0];
        if ( cfx != null ) {
            m += cfx.getMessage();
            stackTrace = cfx.getStackTrace();
        }
        if ( iox != null ) {
            m += "  " + iox.getMessage();
            stackTrace = iox.getStackTrace();
        }
        
        if ( m.length() < 1 ) m += " bad case: cfx and iox are null.";
        ConnectFailureException x = new ConnectFailureException(m);
        x.setStackTrace(stackTrace);
        throw x;
    }

        /** Creates connection and authenticates with a public key
         *
         * @param host
         * @param user
         * @param pemFile
         * @return connection or null if
         * @throws IOException
         * @throws AuthentificationFailedException if
         *  {@link Connection#authenticateWithPublicKey(java.lang.String, java.io.File, java.lang.String)
         *  returns false.
         */
    private static Connection conAuth(String host,String user,File pemFile)
        throws IOException, ConnectFailureException {
        Connection c = null;
        try {           
            c = new Connection(host);
            c.connect(null,ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS,
                           ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS);
            if ( !c.authenticateWithPublicKey(user,pemFile,null) ) {
                c.close();
                throw new ConnectFailureException(
                        "Authorization failed connecting to " + user + "@" + host);
            }
            else return(c);
        }
        catch(IOException iox) {            
            if ( c != null ) c.close();
            throw iox;
        }
    }
    
        /** Creates connection and authenticates with a password
         *
         * @param host
         * @param user
         * @param password
         * @return connection or null if
         * @throws IOException
         * @throws AuthentificationFailedException if
         *  {@link Connection#authenticateWithPublicKey(java.lang.String, java.io.File, java.lang.String)
         *  returns false.
         */
    private static Connection conAuth(String host,String user,String password)
        throws IOException, ConnectFailureException {
        Connection c = null;
        try {           
            c = new Connection(host);
            c.connect(null,ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS,
                           ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS);
            if ( !c.authenticateWithPassword(user, password) ) {            
                c.close();
                throw new ConnectFailureException(
                        "Authorization failed connecting to " + user + "@" + host);
            }
            else return(c);
        }
        catch(IOException iox) {            
            if ( c != null ) c.close();
            throw iox;
        }
    }

        /**
         *
         * @param
         * @return hosts connections keyed by host name;
         */
    public static HashMap<String,Connection> connect(List<String> hosts,
                                                     String user,
                                                     File pemFile,
                                                     long maxWait)
        throws ConnectFailureException {

        HashMap<String,Connection> ht = new HashMap<String, Connection>();

        for ( String h : hosts ) {
            ht.put(h,Ssh.connect(h, user, pemFile,maxWait));
        }
        return(ht);
    }

    public static HashMap<String,Connection> connect(List<String> hosts,
                                                     String user,
                                                     File pemFile,
                                                     long maxWait,
                                                     long maxTotalWait)
        throws ConnectFailureException {

        ConnectFailureException cx = null;

        long t0 = System.currentTimeMillis();
        while ( (System.currentTimeMillis() - t0) < maxTotalWait ) {
            try {
                return( connect(hosts,user,pemFile,maxWait) );
            }
            catch(ConnectFailureException cfx) {
                cx = cfx;
                try { Thread.sleep(1000L); }
                catch(InterruptedException ix) {}
            }
        }

        if ( cx != null ) throw cx;
        else return(null);
    }

    public static void closeConnections(HashMap<String,Connection> ht) {

        for ( Iterator<String> it = ht.keySet().iterator(); it.hasNext(); ) {
            String k = it.next();
            Connection c = ht.get(k);
            if ( c != null ) c.close();
        }
    }

        /** Uses <tt>ch.ethz.ssh2.Connection.connect()</tt> to test whether
         *  the host is reachable (since <tt>InetAddress.isReachable()</tt>
         *  does not work).
         *
         * @param host
         * @return
         */
    public static boolean pingSsh(String host) {
        Connection c = null;
        try {
            c = new Connection(host);
            ConnectionInfo ci =
                c.connect(null,ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS,
                               ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS);
                // Note that without the timeout values ^ connect does
                // not timeout.
                // Note that authentication is not performed.
            return(true);
        }
        catch(Exception iox) {
            if ( c != null ) c.close();
            return(false);
        }
        finally {
            if ( c != null ) c.close();
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
            if ( pingSsh(host) ) return;
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }
        throw new SshPingFailureException("Util.waitForPingSsh() timed out attempting to " +
            " reach " + host);
    }

//    public static void waitForPingsFromAllHosts(String networkID,long maxWaitMillis)
//        throws NoSuchNetworkException, SshPingFailureException {
//
//        NetworkInfo ni = NiM.getForID(networkID);
//        if ( ni == null ) throw new NoSuchNetworkException("Network ID=" +
//                                    networkID + " was not found.");
//
//        List<InstanceStatus> instances = ni.instances;
//        for ( int i = 0; i < instances.size(); i++ ) {
//            waitForPingSsh(instances.get(i).getPublicDnsName(),maxWaitMillis);
////            /* D */ System.out.println("- successful ping from " + instances.get(i).getPublicDnsName());
//        }
//    }

//    public static Pinger pingNetwork(String networkID, long maxWait)
//        throws NoSuchNetworkException {
//
//        NetworkInfo ni = NiM.getForID(networkID);
//        if ( ni == null ) throw new NoSuchNetworkException("Network ID=" +
//                                    networkID + " was not found.");
//
//        List<InstanceStatus> instances = ni.instances;
//
//        Pinger pinger = new Pinger(maxWait);
//        pinger.pingNetwork(instances);
//
//        return(pinger);
//    }

//    public static void main(String[] argv) throws Exception {
//
//        Connection c =
//        Ssh.connect("ec2-184-73-12-43.compute-1.amazonaws.com",
//                    ppe.ec2.Constants.EC2_USERNAME,
//                    new File("/home1/bwd/eh/aws/bw_account/bw_keypair0.pem"),
//                    
//                    5000L);
//        System.out.println("Connected");
//        c.close();
//    }
}
