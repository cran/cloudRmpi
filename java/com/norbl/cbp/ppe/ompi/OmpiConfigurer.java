/*
    Copyright 2012 Northbranchlogic, Inc.

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

import ch.ethz.ssh2.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;
import com.norbl.util.ssh.*;
import java.io.*;
import java.util.*;

/** Supercedes {@link OmpiConfig}.  This version avoid ssh exec as much
 *  as possible.<p>
 *  
 *  Config operations:
 *  <ul>
 *  <li>Create RSA key pair on master node</li>
 *  <li>Install public RSA key on slave nodes</li>
 *  <li>Create the ompi-hostfile and install on master node</li>
 *  <li>Disable hyperthreading on all nodes</li>
 *  <li>Configure rsu (RStudio Server user) on the master node iff it supports
 *      Rstudop server</li>
 *      <ul><li>Install the RSA keypair in ~rsu/.ssh</li>
 *          <li>Copy ompi-hostfile to ~rsu</li>
 *      </ul>
 *  </ul>
 *
 * @author Barnet Wagman
 */
public class OmpiConfigurer {

    public enum State {
        nil,
        configuring,
        configured,
        partiallyConfigured,
        failed
    }
    
    static final long SSH_EX_MAX_WAIT = 1000L * 30L;
    static final String NODE_SHELL_SCRIPT_DIR = "/home/apps";
    
    Ec2Wrangler ec2w;
    NetworkSpec spec;
    ParamsEc2 paramsEc2;
    
    String networkID;
    
    HashMap<String,Connection> connectionHt;
    List<String> hosts;
    String master;
    List<String> slaves;
        
    State state;
    String stateMessage;
    
    boolean ompiHostFileInstalled = false;
    
    
    File localWorkingDir;
    File localTmpDir;    
    
    public OmpiConfigurer(Ec2Wrangler ec2w, 
                      NetworkSpec spec,
                      ParamsEc2 paramsEc2) { //  OmpiSpec spec) {
        this.ec2w = ec2w;
        this.spec = spec;
        this.paramsEc2 = paramsEc2;
        state = State.nil;
        
        localWorkingDir = new File(SysProp.user_home.getVal(),".ompi_tmp_dir");
        if ( !localWorkingDir.exists() || !localWorkingDir.isDirectory() ) {
            localWorkingDir.mkdirs();
            localWorkingDir.deleteOnExit();
        }
        
        localTmpDir = new File(localWorkingDir,"tmp");
        if ( !localTmpDir.exists() || !localTmpDir.isDirectory() ) {
            localTmpDir.mkdirs();
            localTmpDir.deleteOnExit();
        }
    }
    
    public boolean isPending() {                
        return( !State.configuring.equals(state) &&
                !State.nil.equals(state)           
              );
    }
    
    
        /** Just tests the state of this object; this method does not
         *  access the network instances.
         * @return
         */
    public boolean isConfigured() { 
        return(State.configured.equals(state) ||
               State.partiallyConfigured.equals(state));
    }

    public String getName() { return("open-mpi"); }
    public String getStateDescription() {       
        return(state.toString());
    }
    
    public boolean config(String networkID) {
        
        setState(State.configuring);
        
            // The order is critical                
        setNetworkID(networkID);
        
        if ( !connectToNodes() ) {
            setState(State.failed,"Failed to connect to nodes");
            return(false);
        }
        
        PPEApp.verbose("OmpiConfigurer: connected to nodes");
        
            // Create and upload the ompi host file
        ompiHostFileInstalled = createAndUploadOmpiHostFile();
        if ( !ompiHostFileInstalled ) {
            setState(State.failed,"Failed to install ompi host file");
            return(false);
        }
        
        PPEApp.verbose("OmpiConfigurer: ompi host file installed.");
        
            // Create, upload and execute the master node config script
            // This includes creating the RSA keypair, so this script
            // must run before we configure the slaves
        String masterNodeScript = buildMasterNodeShellScript();
        try {
            PPEApp.verbose("master node script:\n" + masterNodeScript);
            execScript(master,masterNodeScript,true);
        }
        catch(Exception xxx) {
            PPEApp.verbose(xxx);
            setState(State.failed,"Exec of the master node shell script failed.");
            return(false);
        }
        
        PPEApp.verbose("OmpiConfigurer: master node configured.");
        
            // Upload the public keys to the slaves.
        if ( !uploadPublicRSAKeyToSlaves() ) {
            setState(State.failed,"Failed to install the public RSA key " +
                     "on slave nodes.");
            return(false);
        }
        PPEApp.verbose("OmpiConfigurer: public RSA keys installed.");
        
            // Create, upload and execute the slave node config scripts
        int nSlavesConfigured = configureSlaveNodes();
        if ( (slaves.size() < 1) ||
             (nSlavesConfigured >= slaves.size()) ) {
            setState(State.configured);
            return(true);
        }
        else if ( nSlavesConfigured < slaves.size() ) {
            setState(State.failed,
                     nSlavesConfigured + "/" + slaves.size() +
                    " slave nodes were configured.");
            return(false);
        }
        else {
            setState(State.failed,"Bad case.");
            return(false);
        }                               
    }
    
    public void setNetworkID(String networkID) { this.networkID = networkID; }
    
        // ------- Com methods -----------------------------
    
    public boolean connectToNodes() {
        try {
            if ( networkID == null ) 
                throw new Exception("The network ID is missing");            

            NetworkInfo ni = NiM.getForID(networkID);

            master = ec2w.getMasterPublicDns(ni);    
            if ( master == null ) 
                throw new Exception("Can't get the " +
                                    "master node's public DNS");
            slaves = ec2w.getSlavesPublicDns(ni);
            if ( slaves == null ) 
                throw new Exception("Failed to get slave node public URLs ");

            hosts = new ArrayList<String>();
            hosts.add(master);
            hosts.addAll(slaves);

                // Wait until we can ping them all.
            waitForAllHostsToRespond(hosts);

                // Connect waiting a long time.
            connectionHt = Ssh.connect(hosts,ConstantsEc2.EC2_USERNAME,
                                paramsEc2.rsaKeyPairFile, // spec/rsaKeyPairFile,
                                1000L * 10L,
                                1000L * 60L * 10L);

                // Make sure that all the connections are there
            if ( connectionHt.get(master) == null )
                throw new Exception("No connection for the master node " +
                                    master);
            for ( String sl : slaves ) {
                if ( connectionHt.get(sl) == null ) {
                    GuiUtil.warning(
                        new String[] { "No connection for node " + sl},
                        "No connection");
                }
            }
            
            return(true);
        }   
        catch(Exception xxx ) {
            GuiUtil.exceptionMessage(xxx);
            return(false);
        }
    }
    
        // -------- Bash script builder --------------------
    
    String buildMasterNodeShellScript() {
        
        StringBuilder s = new StringBuilder();
        
            // Specify creating the RSA keypair
        s.append("cd " + ConstantsEc2.EC2_USER_SSH_DIR + ";\n");
        s.append("ssh-keygen -t rsa -f " + 
                  ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME + 
                  " -P '';\n"); 
        s.append("\n");
        
            // Get the hyperthread disabling commands
        try {
            s.append(getHyperThreadDisablingCommands(master));
            s.append("\n");
        }
        catch(Exception xxx) {
            PPEApp.verbose(xxx);            
        }
        
            // Get the rsu config commands
        s.append(getRsuCommands());
        
        s.append("\n");
        
        return(s.toString());
    }
    
    String buildSlaveNodeShellScript(String slave) {
        
        StringBuilder s = new StringBuilder();
        
            // Append public key to authorized file
        s.append("cd " + ConstantsEc2.EC2_USER_SSH_DIR + "\n" );
        s.append("cat " + getPublicKeyFilename() + 
                  " >> " +
                  ConstantsOmpi.AUTHORIZED_KEYS_FILENAME + "\n");
        s.append("\n");
        
            // Get the hyperthread disabling commands
        try {
            s.append(getHyperThreadDisablingCommands(slave));
            s.append("\n");
        }
        catch(Exception xxx) { PPEApp.verbose(xxx); }
                
        return(s.toString());
    }
    
        // ---------------------------------------------------
    
    
     boolean createAndUploadOmpiHostFile() {
        
        try { 
                // Create the file locally
            File hostFile = createHostFile(hosts);

                // Upload to master
            SshCp scp = new SshCp();
            scp.cp(connectionHt.get(master),
                   ConstantsEc2.EC2_USER_HOME_DIR,hostFile);
            return(true);
        }
        catch(Exception xxx) {
            GuiUtil.exceptionMessage(xxx);
            return(false);
        }
     }
  
     boolean uploadPublicRSAKeyToSlaves() {
         try {
            SshCp scp = new SshCp();
            scp.cp(connectionHt,slaves, master,
                    ConstantsEc2.EC2_USER_SSH_DIR,
                    getPublicKeyFilename());
            return(true);
         }
         catch(Exception xxx) {
             GuiUtil.exceptionMessage(xxx);
             return(false);
         }         
     }
     
     String getPublicKeyFilename() {
         return(ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME + ".pub" );
     }
     
     String getHyperThreadDisablingCommands(String nodeName) 
         throws Exception {
         
         Connection con = connectionHt.get(nodeName);
         if ( con == null ) throw new Exception("No connection for " + nodeName);
         
         SshExec sx = new SshExec(con);
         
            // Get the list of 'cpus'
         String cmd =
            "sudo ls -1d /sys/devices/system/node/node0/cpu[0123456789]*/online";        
                  
         String cpuS = sx.execRead(cmd, SSH_EX_MAX_WAIT);
         if ( cpuS == null ) throw new Exception("Failed to get " +
                             " cpu list from " + nodeName);
         
         String[] cpuLines = cpuS.trim().split("\n");
         if ( cpuLines.length < 1 ) return("");
                  
            // Get the the state numbers
         cmd = 
   "sudo cat `ls -1d /sys/devices/system/node/node0/cpu[0123456789]*/online`";
        
         String stateS = sx.execRead(cmd, SSH_EX_MAX_WAIT);
         if ( stateS == null ) throw new Exception("Failed to get " +
                             " cpu state list from " + nodeName);
         
         String[] stateLines = stateS.trim().split("\n");
         if ( stateLines.length < 1 ) return("");
         
         if ( stateLines.length != cpuLines.length )
             throw new Exception("cpu/state count mismatch.");
         
         int nDisableable = countDisableable(stateLines);
         
            // Select the 'cpus' to disable
         int nCores = spec.slotsPerHost;
         if ( nCores < 1 ) return("");
         
         int nToDisable = nDisableable - nCores;
         if ( nToDisable < 1 ) return("");
                 
         StringBuilder s = new StringBuilder();
         int nDisabled = 0;
         
         NEXT: for ( int i = 0; i < cpuLines.length; i++ ) {
             
            if ( !stateLines[i].trim().equals("1") ) continue NEXT;
            
            String c = "echo 0 | sudo tee " + cpuLines[i] + "\n";
            s.append(c);           
            ++nDisabled;
            
            if ( nDisabled >= nToDisable ) return(s.toString());
         }
                
         return(s.toString());         
     }
     
     // echo 0 | sudo tee /sys/devices/system/node/node0/cpu11/online    
     
     private int countDisableable(String[]  stateLines) {
         
         int n = 0;
         for ( String s : stateLines ) {
             if ( s.trim().equals("1") ) ++n;
         }
         return(n);
     }
     
     public String getRsuCommands() {
         
         StringBuilder s = new StringBuilder();
         
         String targPN = ConstantsEc2.RSU_USER_HOME_DIR + "/" +
                         ConstantsOmpi.OMPI_HOSTFILE_NAME;
         
         s.append("cd " + NODE_SHELL_SCRIPT_DIR + "\n");
                          
         // All rsu commands are prefixed with sudo and a test
         // for the existence of /home/rsu.  The latter should 
         // prevent problems with non-rs ami.
         String pre = "[ -e /home/rsu ] && ";
         
            // Copy the ompi host file
         s.append(pre + "cp " + 
                  ConstantsEc2.EC2_USER_HOME_DIR + "/" +
                  ConstantsOmpi.OMPI_HOSTFILE_NAME + " " +
                  targPN + ";\n");
         
         s.append(pre + "chown rsu " + targPN + "\n");
         s.append(pre + "chgrp rsu " + targPN + "\n");        
         s.append(pre + "chmod a+rw " + targPN + "\n");
         s.append("\n");
         
            // Copy the rsakeypair file
         String kp = ConstantsEc2.EC2_USER_SSH_DIR + "/" +
                     ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME;
                   
         String targKP = ConstantsEc2.RSU_USER_SSH_DIR + "/" +
                         ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME;
                           
         s.append(pre + "mkdir " + ConstantsEc2.RSU_USER_SSH_DIR + "\n");
         s.append(pre + "chown rsu " + ConstantsEc2.RSU_USER_SSH_DIR + "\n");
         s.append(pre + "chgrp rsu " + ConstantsEc2.RSU_USER_SSH_DIR + "\n");
         s.append(pre + "chmod go-rwx " + ConstantsEc2.RSU_USER_SSH_DIR + "\n");
         
         s.append(pre + "cp " + kp + " " + ConstantsEc2.RSU_USER_SSH_DIR + "\n");
         s.append(pre + "chown rsu " + targKP + "\n");
         s.append(pre + "chgrp rsu " + targKP + "\n");
         s.append(pre + "chmod go-rw " + targKP + "\n");
         
         s.append("\n");
         
         return(s.toString()); 
     }
     
     void uploadShellScript(String nodeName,
                            String shellScript) 
         throws Exception {
         
         Connection con = connectionHt.get(nodeName);
         if ( con == null ) throw new Exception("No connection for " + nodeName);
         SshExec sx = new SshExec(con);
         
         
     }
     
     void execScript(String nodeName,String shellScript,boolean asRoot) 
         throws Exception {
         
            // Write script to local file.
         String fn = nodeName + ".sh";
         File localFile = new File(localWorkingDir,fn);
         int n = FileUtil.write(localFile,shellScript);
         if ( n < 0 ) throw new Exception("Local write to " +
                                          localFile.getPath() + " failed.");
         
            // Upload the file
         Connection con = connectionHt.get(nodeName);
         if ( con == null ) throw new Exception("No connection for " + nodeName);
         
         SshCp scp = new SshCp();
         scp.cp(con,NODE_SHELL_SCRIPT_DIR,localFile);
         
            // Make sure it's there
         scp.download(con, localTmpDir.getPath(), 
                      NODE_SHELL_SCRIPT_DIR + "/" + fn);
         File ck = new File(localTmpDir,fn);
         if ( !ck.exists() ) throw new Exception("Failed to up load shell " +
                                " script " + shellScript + " to " + nodeName);
         
            // Exec it
         String cmd = "cd " + NODE_SHELL_SCRIPT_DIR + "; ";
         if ( asRoot ) {
             cmd += "sudo bash -c 'bash " + fn + "'";
         }
         else {
            cmd += "bash " + fn;
         }
         
         PPEApp.verbose(cmd);
         
         SshExec sx = new SshExec(con);
         String r = sx.execRead(cmd, SSH_EX_MAX_WAIT);
         // ^ We us execRead to 'wait' for the script to finish,
         //   BUT with a time out.  We don't care about the returned value.
         
//         PPEApp.verbose("Exec'd " + cmd + " -> " + r);
     }
     
     private void setState(State state) {
         setState(state,null);
     }
     
     private void setState(State state, String message) {        
        this.state = state;
        NiM.fireStateChangeEvent();
        stateMessage = message;
     }
     
     class SlaveNodeConfigurer implements Runnable {
         
         String hostName;
         boolean done = false;
         Exception xxx;
         
         SlaveNodeConfigurer(String hostName) {
             this.hostName = hostName;
         }
         
         public void run() {
             try {
                String sc = buildSlaveNodeShellScript(hostName);  
                PPEApp.verbose("Slave node script for " + hostName + "\n" +
                               sc + "\n");
                execScript(hostName, sc,true);
                done = true;
             }
             catch(Exception xx) {
                 PPEApp.verbose(xx);
                 this.xxx = xx;
                 done = true;
             }                          
         }         
     }
     
     /** 
      * 
      * @return 
      */
     int configureSlaveNodes() {
         
         List<SlaveNodeConfigurer> sncs = new ArrayList<SlaveNodeConfigurer>();
         
         for ( String hostName : slaves ) {
             SlaveNodeConfigurer snc = new SlaveNodeConfigurer(hostName);
             sncs.add(snc);
             (new Thread(snc)).start();
         }
         
         PPEApp.verbose("configureSlaveNodes: " + sncs.size() +
                        " threads launched");
         
         long tmMax = System.currentTimeMillis() +
                      (1000L * 60L * 5L);
         while ( (getNSlaveNodeConfigurersFinished(sncs) < sncs.size()) &&
                 (System.currentTimeMillis() < tmMax) ) {
             try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
         }
         
         PPEApp.verbose("Configured " +
                 getNSlaveNodeConfigurersFinished(sncs) + "/" +
                 sncs.size() + " slave nodes.");
         
         return(getNSlaveNodeConfigurersFinished(sncs));
     }
     
     int getNSlaveNodeConfigurersFinished(List<SlaveNodeConfigurer> sncs) {
         int n = 0;
         for ( SlaveNodeConfigurer snc : sncs ) {
             if ( snc.done ) ++n;
         }
         return(n);
     }
       
        // --------------------------------------------------------
    
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
}

