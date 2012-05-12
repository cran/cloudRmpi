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

package com.norbl.cbp.ppe.ompi;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.ssh.*;
import java.util.*;
import java.io.*;
import ch.ethz.ssh2.*;

/** After the ec2 instances are booted, this class is used
 *  to configure Open MPI.  Specifically it:
 *
 *  <ul>
 *  <li>Creates an rsa keypair on the master</li>
 *  <li>Copies the rsa keypair to the slaves and appends it to their
 *      <tt>.ssh/authorized_keys</tt> files.</li>
 *  <li>Creates a host file and puts it on the master.</li>
 *  </ul>
 *
 * @author Barnet Wagman
 * @deprecated 
 */
public class OmpiConfig {

    public enum State {
        nil("-"),
        configuring("configuring ompi"),
        connectingToInstances("connecting to instances"),
        pingingHosts("pinging hosts"),
        pingFailed("Ping failed"),
        configuringIntraNetworkAccess("configuring intra-network access"),
        creatingHostfile("creating hostfile"),
        disablingHyperthreading("disabling hyperthreading") ,
        isConfigured("configured"),
        notConfigured("not configured");
      
        String title;
        State(String title) { this.title = title; }
        public String getTitle() { return(title); }
        public void appendToTitle(String a) {
            title += a;
        }
    }

    State state;

    Ec2Wrangler ec2w;
//    OmpiSpec spec;
    NetworkSpec spec;
    ParamsEc2 paramsEc2;

    public OmpiConfig(Ec2Wrangler ec2w, 
                      NetworkSpec spec,
                      ParamsEc2 paramsEc2) { //  OmpiSpec spec) {
        this.ec2w = ec2w;
        this.spec = spec;
        this.paramsEc2 = paramsEc2;
        state = State.nil;
    }

    private void setState(State state) {        
        this.state = state;
        NiM.fireStateChangeEvent();
    }

    public boolean isPending() {
        return( !State.isConfigured.equals(state) &&
                !State.notConfigured.equals(state)           
              );
    }
  
//    public boolean isPending() {
//        return( ( State.configuringIntraNetworkAccess.equals(state) ||
//                  State.connectingToInstances.equals(state) ||
//                  State.creatingHostfile.equals(state) ||
//                  State.disablingHyperthreading.equals(state) ||
//                  State.configuring.equals(state) ||
//                  State.pingingHosts.equals(state) )
//               );
//    }

        /** Just tests the state of this object; this method does not
         *  access the network instances.
         * @return
         */
    public boolean isConfigured() { return(State.isConfigured.equals(state)); }

    public String getName() { return("open-mpi"); }
    public String getStateDescription() {       
        return(state.getTitle());
    }

    public int getPort() { return(-1); }

    public boolean config(String networkID) {

        NetworkInfo ni = NiM.getForID(networkID);
    
        HashMap<String,Connection> htc = null;
        try {
            setState(State.configuring);
            String master = ec2w.getMasterPublicDns(ni);
           
            List<String> slaves = ec2w.getSlavesPublicDns(ni);
            
            PPEApp.verbose("# OmpiConfig.config # " +
                           " master=" + master + " n slaves=" + slaves.size());
            
                // Make the connections
            setState(State.pingingHosts);
            List<String> hosts = new ArrayList<String>();
            hosts.add(master);
            hosts.addAll(slaves);
            state.appendToTitle(" n=" + Integer.toString(hosts.size()));

                // Wait until we can ping them all.
            waitForAllHostsToRespond(hosts);
            
            PPEApp.verbose("# OmpiConfig.config # " +
                      " all " + hosts.size() + " hosts responded");
           
            setState(State.connectingToInstances);
                // Connect waiting a long time.
            htc = Ssh.connect(hosts,ConstantsEc2.EC2_USERNAME,
                              paramsEc2.rsaKeyPairFile, // spec/rsaKeyPairFile,
                              1000L * 10L,
                              1000L * 60L * 10L);
            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " connected to hosts");
            
            setState(State.configuringIntraNetworkAccess);
            CmdExecutor sxMaster = new CmdExecutor(htc.get(master));
            SshCp scp = new SshCp();

                // Create the rsa file on the master
            sxMaster.createRSAKeypair();
            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " created RSA");
              
                // Copy the key pair file to rsu
            sxMaster.copyRSAKeyPairFileToRsu();
                        
            PPEApp.verbose("# OmpiConfig.config # " +
                        " copied RSA to rsu");
            
                // Copy the public key file to the slaves    
            String publicKeyFilename =
                    ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME + ".pub";
            scp.cp(htc,slaves, master,
                   ConstantsEc2.EC2_USER_SSH_DIR,publicKeyFilename);
            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " copied keys to slaves");

                // Append the public key file to the authorized_keys
                // file on the slaves
           
            for ( String slave : slaves ) {
                SshExec sx = new SshExec(htc.get(slave));
                sx.exec("cd " + ConstantsEc2.EC2_USER_SSH_DIR + " ; " +
                        "cat " + publicKeyFilename + " >> " +
                         ConstantsOmpi.AUTHORIZED_KEYS_FILENAME,
                        1000L * 10L); 
            }
          
            PPEApp.verbose("# OmpiConfig.config # " +
                        " appended keys on slaves");
            
                // Create the hostfile and install it on the master
            setState(State.creatingHostfile);
            List<String> hostNames = new ArrayList<String>();
            hostNames.add(master);
            hostNames.addAll(slaves);
            File hostFile = createHostFile(hostNames);
           
            PPEApp.verbose("# OmpiConfig.config # " +
                        " created hostfile " + hostFile);
            
            scp.cp(htc.get(master),ConstantsEc2.EC2_USER_HOME_DIR,hostFile);
            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " uploaded hostfile");
            
                // Copy the host file to rsu
            sxMaster.copyOmpiHostFileToRmu();
            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " copied host file to rsu");
            
            hostFile.delete();

            
            PPEApp.verbose("# OmpiConfig.config # " +
                        " local copy of host file deleted. " +
                    "disable hyperthreading: " + spec.disableHyperThreading);
            
                // If specified, disable hyperthreading
            if ( true) { // spec.disableHyperThreading ) { /* D */
                PPEApp.verbose("# OmpiConfig.config # " +
                        " disabling hyperthreading for " + hostNames.size());
                setState(State.disablingHyperthreading);
                for ( String host : hostNames ) {
                    HyperthreadDisabler htd = new HyperthreadDisabler();
                    SshExec sx = new SshExec(htc.get(host));
                    htd.disable(sx,spec.slotsPerHost);
                    PPEApp.verbose("# OmpiConfig.config # " +
                                " disabled ht on " + host);
                }
                PPEApp.verbose("# OmpiConfig.config # " +
                            "  disabled hyper threading");
            }   
            else PPEApp.verbose("# OmpiConfig.config # " +
                    " NOT disabling hyperthreading");
//            
//            if ( spec.disableHyperthreading ) {
//                setState(State.disablingHyperthreading);
//                Ec2InstanceType iit = spec.instanceType
//                        Ec2InstanceType.getInstanceInfo(spec.instanceType);
//                if ( iit == null ) throw new RuntimeException("iit == null");
//                for ( String host : hostNames ) {
//                    HyperthreadDisabler htd = new HyperthreadDisabler();
//                    SshExec sx = new SshExec(htc.get(host));                   
//                    htd.disable(sx,iit.nCores);
//                }
//            }

            setState(State.isConfigured);
            return(true);
        }
        catch(SshPingFailureException px) {
            setState(State.pingFailed);
            ExceptionHandler.text(px);
            return(false);
        }
        catch(Exception xxx) {
            setState(State.notConfigured);
            ExceptionHandler.text(xxx);
            return(false);
        }
        finally {
            if ( htc != null ) Ssh.closeConnections(htc);
        }
    }

    private void waitForAllHostsToRespond(List<String> hosts)
        throws SshPingFailureException {
        List<String> failures = new ArrayList<String>();
        for ( String h : hosts ) {
            try {
                Ssh.waitForPingSsh(h,ConstantsSSH.MAX_WAIT_FOR_SSH_PING);
            }
            catch(SshPingFailureException px) {
                failures.add(h);
            }
        }
        if ( failures.size() > 0 ) {
            String s = new String();
            for ( String h : failures ) {
                s += h + " ";
            }            
            throw new SshPingFailureException("Failed to contact hosts: " +
                                              s.trim());
        }
    }

        // ----------------------------------------------


        /** Creates the host file on the local system.
         *
         * @return
         */
    private File createHostFile(List<String> hostNames) throws IOException {

        StringBuilder s = new StringBuilder();
        s.append("# " + ConstantsOmpi.OMPI_HOSTFILE_NAME + "\n\n");

        for ( String host : hostNames ) {
            s.append(host +
                    " slots=" + Integer.toString(spec.slotsPerHost) + "\n");
        }

        String txt = s.toString();

            // Write the host file to the tmp dir.
        File tmpDir = FileUtil.getTmpDir(ConstantsPPE.TMP_DIR);
        File hostfile = new File(tmpDir,ConstantsOmpi.OMPI_HOSTFILE_NAME);

        FileWriter fw = new FileWriter(hostfile);
        fw.write(txt);
        fw.flush();
        fw.close();
        return(hostfile);
    }

    private File constructRemoteRSAKeyPairFile() {
        return(new File("/home/" +
                        ConstantsEc2.EC2_USERNAME +
                        "/.ssh/" +
                        ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME));
    }

    private File constructRemoteOmpiHistFile() {
        return(new File("/home/" +
                        ConstantsEc2.EC2_USERNAME +
                        "/" +
                        ConstantsOmpi.OMPI_HOSTFILE_NAME));
    }

        // ----------------------------------------------

    class CmdExecutor extends SshExec {

        CmdExecutor(Connection connection)
                     throws IOException {
            super(connection);
        }

        /** Creates an rsa keypair in <tt>~ec2-user/.ssh</tt> with
         *  the filename specified by {@link Constants#EC2_MPI_MASTER_KEY_PAIR_FILENAME}.
         *  This command produces both the keypair file and
         *  a public key file, which has the same filename with ".pub"
         *  appended.
         * @return
         */
        public void createRSAKeypair() throws IOException {

            String dir = ConstantsEc2.EC2_USER_SSH_DIR;
            String fn = ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME;
            String cmd = "cd " + dir + "; " +
                          "ssh-keygen -t rsa -f " + fn + " -P '';"; 
            /* D */ System.out.println("# OmpiConfig.createRSAKeyPair # " +
                        " cmd=" + cmd);
            exec(cmd,1000L * 10L);         
        }
        
        public void copyRSAKeyPairFileToRsu() {
            PPEApp.verbose("# OmpiConfig.copy RSA ... RSU # ");
            try { // Catch and ignore exceptions.
                String src = ConstantsEc2.EC2_USER_SSH_DIR + "/" +
                            ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME;

                String targ = ConstantsEc2.RSU_USER_SSH_DIR + "/" +
                            ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME;

                execNX("sudo mkdir " + ConstantsEc2.RSU_USER_SSH_DIR);
                
                execNX("sudo chown rsu " + ConstantsEc2.RSU_USER_SSH_DIR);
                execNX("sudo chgrp rsu " + ConstantsEc2.RSU_USER_SSH_DIR);
                execNX("sudo chmod go-rwx " + ConstantsEc2.RSU_USER_SSH_DIR);
                
                execNX("sudo cp " + src + " " + targ);
                
                    // Change ownership
                execNX("sudo chown rsu " + targ);
                execNX("sudo chgrp rsu " + targ);
            }
            catch(Exception xxx) {
                /* D */ System.err.println("xxx");
            }            
        }
        
        void execNX(String cmd) {
            try {
                exec(cmd,1000L * 10L);
                PPEApp.verbose("OmpiConfig...execNX: " +  cmd );
            }
            catch(Exception xxx) { System.out.println(xxx); }
        }
        
        public void copyOmpiHostFileToRmu() {
            try { // Catch and ignore exceptions
                
                String src = ConstantsEc2.EC2_USER_HOME_DIR + "/" +
                            ConstantsOmpi.OMPI_HOSTFILE_NAME;
                String targ = ConstantsEc2.RSU_USER_HOME_DIR + "/" +
                            ConstantsOmpi.OMPI_HOSTFILE_NAME;
                
                exec("sudo cp " + src + " " + targ,1000L * 10L);
                    
                    // Change ownership
                exec("sudo chown rsu " + targ,1000L * 10L);
                exec("sudo chgrp rsu " + targ,1000L * 10L);
            }
            catch(Exception xxx) {
                PPEApp.verbose(xxx);                
            }
        }
    }            
}
