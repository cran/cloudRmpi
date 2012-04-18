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

import com.amazonaws.*;
import com.norbl.util.*;
import java.lang.reflect.*;
import com.norbl.util.gui.*;
import com.norbl.cbp.ppe.gui.*;
import com.norbl.cbp.ppe.usermonitor.*;
import com.norbl.util.aws.*;
import javax.swing.*;
import java.util.*;
import com.amazonaws.services.ec2.model.*;

/** Launches and manages a network of ec2 instances. This class provides
 *  the operations but does <i>not</i> contain any gui code or references
 *  to gui code,except modal and faux modal warnings.
 *  Network manager guis use concrete subclasses of this
 *  class.
 *  <p>
 *  Note that this class does not do anything related to mpi.  mpi 
 *  configuration handled via {@link com.norbl.cbp.ppe.Services},
 *  which are specified in {@link #specifyServicers}.
 *  
 *
 * @author Barnet Wagman
 */
abstract public class PPEManager {
    
    public ParamHt paramHt;
    public static ParamsEc2 paramsEc2;
    public Ec2Wrangler ec2w;
    public AwsClientConfigParams awsClientConfigParams;

    public NetworkTableModel networkTableModel;
    public InstanceTableManager instanceTableManager;
    
    NiUpdater niUpdater;    
    
    public List<Ec2InstanceType> instanceTypes;    
    
    String amiGroupName;
    public List<AmiPPE> amis;
    
    public Region ec2Region;
    public List<AvailabilityZone> availabilityZones;
    
    public EbsVolumeWrangler ebsVolumeWrangler;
    private EbsVolumeListFrame ebsVolumeListFrame;
    
    /** Use as parent of dialogs.  This need not be set - null is ok.
     * 
     */
    JFrame networkManagerFrame;

    /**
     * 
     * @param argv
     * @param amiGroupName
     * @param ec2Region Because clusters are only supported in
     *                  a US-EAST, we only need on region.  If this
     *                  changes, we may want to give users a choice.
     * @throws Exception 
     */
    public PPEManager(String[] argv, 
                      String amiGroupName,
                      Region ec2Region) throws Exception {
                
        paramHt = new ParamHtPPE(argv);
        paramsEc2 = new ParamsEc2(paramHt);
        this.amiGroupName = amiGroupName;
        this.ec2Region = ec2Region;
        awsClientConfigParams = new AwsClientConfigParams(this);
               
        NiM.init();
        networkTableModel = new NetworkTableModel();
        instanceTableManager = new InstanceTableManager(ebsVolumeWrangler);
        
        setupAWSWrangling();           
    }
    
    /** Sets up things that require access to aws, such as the
     *  {@link Ec2Wrangler}.  This method requires that aws
     *  params are available.  It uses {@link ParamsEc2#hasRequiredLaunchParams())
     *  to check and does nothing if is returns false.
     * 
     */
    public boolean setupAWSWrangling() throws MissingParamsException {
                
        if ( !paramsEc2.hasRequiredEc2LaunchParams() ) return(false);
        
        ec2w = createWrangler();
        ec2w.ec2Client.setEndpoint(ec2Region.getEndpoint());
        
        availabilityZones = 
            ec2w.ec2Client.describeAvailabilityZones().getAvailabilityZones();
                 
        specifyServices();
               
        NiM.addStateChangeListener(networkTableModel);
        NiM.addStateChangeListener(instanceTableManager);
        
        startNetworkInfoUpdater();
        
        ebsVolumeWrangler = new EbsVolumeWrangler(ec2w.ec2Client);
        ebsVolumeWrangler.retrieveVolumeInfo();        
        instanceTableManager.setEbsVolumeWrangler(ebsVolumeWrangler);
        
        try {
            instanceTypes = Ec2InstanceType.getInstanceTypes();            
            amis = AmiPPE.retrieveAndInitAmiPPEs(new S3Access(),
                                                 amiGroupName,
                                                 ec2w.ec2Client);            
            for ( AmiPPE ami : amis ) ami.region = ec2Region;
            return(true);
        }
        catch(Exception xxx) {
                                    
            GuiUtil.warning(new String[] {
                "Failed to retrieve instance types or AMI descriptions from S3.",
                "This usually means that you are not connected to the internet",
                " ",
                xxx.toString()
            },
            "Error");
            return(false);                            
        }       
    }
    
    private Ec2Wrangler createWrangler() throws MissingParamsException {
        ClientConfiguration cc = null;
        if ( (awsClientConfigParams != null) &&
              awsClientConfigParams.size() > 0 ) {
            cc = awsClientConfigParams.buildClientConfiguration();            
        }               
        return(new Ec2Wrangler(paramsEc2,cc));
    }
    
    public void setNetworkManagerFrame(JFrame f) {
        networkManagerFrame = f;
    }
    
    /** Tests whether aws dependent setup is complete.
     * 
     * @return 
     */
    public boolean goodToGo() {
        return( (paramsEc2.hasRequiredEc2LaunchParams()) && 
                (ec2w != null) );
    }
    
    boolean goodToGoShowWarning() {
        if ( goodToGo() ) return(true);
        else {
            MessageDialog.showNotGoodToGoWarning(networkManagerFrame, this);
            return(false);
        }
    }
    
    abstract protected void specifyServices();
    abstract protected void initializeServices(
            NetworkSpec networkSpec,Services services) throws Exception;
    abstract protected NetworkSpec getFullySpecifiedNetworkSpec(String networkName);
    abstract protected String getAboutAppTitle();
    abstract protected String getAboutAppVersion();
    
    public void updateParamsFromConfigFile() {
        try {
            if ( !paramHt.reloadParams() ) {
                GuiUtil.warning(new String[] {
                        "There is no param file, so params cannot be update.",
                        "Edit the ec2 params (Edit -> Ec2 parameters"},
                         "No param file");
            }
            else {
                paramsEc2 = new ParamsEc2(paramHt);
                if ( paramsEc2.hasRequiredEc2LaunchParams() ) {                               
                    if ( (ec2w != null) && (ec2w.ec2Client == null) )
                        ec2w.ec2Client.shutdown();
                    ec2w = createWrangler();                          
                }
            }
        }
        catch(Exception xx) { GuiUtil.exceptionMessage(xx); }
    }
    
    public NetworkTableModel getNetworkTableModel() {
        return(networkTableModel);
    }
    
    public void updateParamsReBillingAuthorizationID(String ema) throws Exception {        
        paramHt.put(ParamsEc2.ParamName.uid.toString(),ema);       
        paramHt.saveAndReload(false);
        paramsEc2 = new ParamsEc2(paramHt);
    }
    
          // -------- Ops -----------------------------------        
    
    public void doOp(ActionCommandNetworkManager ac) {             
        (new OpThread(ac)).start();   
    }
    
    
          /* Operation are usually triggered by a user via a gui.
           * Since we do not want them performed in the aws event dispatching 
           * thread, these methods create threads to perform the operation.
           * Each of these public op methods creates a and launches
           * an instance of OpThread, which will call a method that
           * performs the specified op.
           */
    
           // ----------- Public op methods -----------------------       
   
    /** Runs an operation (specified by an action command) in a separate thread.
     *  The ActionCommand cmd is assumed to be a unique method name
     *  in this class.
     * 
     */       
    private class OpThread extends Thread {
      
        ActionCommandNetworkManager ac;
        
        OpThread(ActionCommandNetworkManager ac) {
            this.ac = ac;            
        }
                
        public void run() {            
            try {               
                Method m;
                if ( ac.hasNetworkID() )
                    m = PPEManager.class.getMethod(ac.cmd, new Class[] { String.class });
                else
                    m = PPEManager.class.getMethod(ac.cmd,new Class[] {});
                             
                Class[] paramTypes = m.getParameterTypes();
                Object[] params;
                if ( paramTypes.length == 0 ) {
                    params = new Object[] {};
                }
                else {
                    params = new Object[] { ac.networkID };
                }                
                m.invoke(PPEManager.this, params);                
            }
            catch(Exception xx) {                
                GuiUtil.exceptionMessage(xx); 
            }
        }        
    }
        
        // -------- op methods ------------------------
    
    public enum Op {
        
        createNetwork("Create network"), 
        updateNetworkInfo("Update network information"),
        
        editEc2Parameters("EC2 parameters"),
        editAwsClientParameters("AWS client parameters"),
        authorizeInstanceBilling("Authorize instance billing"),        
        cancelInstanceBilling("Cancel authorization for instance billing"),
        
        showAbout("About"),        
        exit("Exit"),
        
        rebootInstances("Reboot instances"),        
        terminateInstances("Terminate instances"),
        
        listEbsVolumes("Show EBS volumes"),
        updateEbsVolumeList("Update list of EBS volumes");
        
        public String textMi;
        Op(String textMI) { this.textMi = textMI; }            
    }
    
    public void createNetwork() throws Exception {
        
        if ( !goodToGoShowWarning() ) return;
        
        InProgressFrame.showInProgress();
        
        String networkName = ec2w.chooseNetworkName(paramsEc2);
        
            // Get the network spec, quit if things are missing
        final NetworkSpec networkSpec = 
                getFullySpecifiedNetworkSpec(networkName);        
        if ( networkSpec == null ) return;               
        
        if ( !networkSpec.isComplete() )
            throw new IncompleteNetworkSpecException(
                            networkSpec.listMissingParams());
        
            // This will take a while, so put it in it's own thread.
        (new Thread() { public void run() {
            try {
                    // Create the network                                        
                String networkID = ec2w.createNetwork(networkSpec,instanceTypes);
                NetworkPinger pinger = new NetworkPinger(ec2w);
                if ( !pinger.pingNetwork(networkID) ) {
                        // There are NO usable instance, so stop now.
                    GuiUtil.warning(
                            new String[] {
                                "There are NO usable instances.",
                             },
                             "No usable instances");
                    return;
                }

                NetworkInfo ni = NiM.getForID(networkID);
                if ( ni == null ) throw new RuntimeException("NO " +
                     " network info for ID=" + networkID);
                Services s = ni.getServices(); 
                initializeServices(networkSpec,s);
                s.launch();
                NiM.update(ec2w);
            }
            catch(Exception xxx) {
                ExceptionHandler.gui(xxx);
                GuiUtil.warning(new String[] {
                    "The network may not have been configured."
                    },
                    "Warning");                    
            }
         }}).start();
    }
    
    public void rebootInstances(String networkID) throws Exception {
        
       if ( !goodToGoShowWarning() ) return;
        
       NetworkInfo ni = NiM.getForID(networkID);
        if ( ni == null ) return;
        if ( GuiUtil.answerIsYes(
            new String[] { "Reboot all instances in network",
                           ni.getNetworkName() + " ?" },
            "Reboot instances") ) {
            ec2w.rebootInstances(ni);
            NiM.update(ec2w);
        }
    }
    
    public void terminateInstances(String networkID) {
        
        if ( !goodToGoShowWarning() ) return;
        
        NetworkInfo ni = NiM.getForID(networkID);
        if ( ni == null ) return;
        if ( GuiUtil.answerIsYes(
            new String[] { "Terminate all instances in network",
                            ni.getNetworkName() + " ?" },
            "Terminate instances") ) {
            ec2w.terminateInstances(ni);               
            NiM.update(ec2w);
        }
    }
    
    public void updateNetworkInfo() {
       if ( !goodToGoShowWarning() ) return;
        NiM.update(ec2w);
    }
    
    public void exit() {
        System.exit(0);
    }
    
    public void showAbout() {       
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {              
                AboutGui ag = new AboutGui();
                ag.setTitle(getAboutAppTitle());
                ag.setVersion(getAboutAppVersion());
                GuiUtil.centerOnScreen(ag);
                SwingDefaults.setIcon(ag);;
                ag.setVisible(true);            
            }
        });
    }
    
    public void authorizeInstanceBilling() {        
        AuthorizationClient ac = new AuthorizationClient(this);
        ac.runAuthorizationDialog();        
    }
    
    public void cancelInstanceBilling() {
        if ( paramsEc2.uid != null ) {
            S3Access s3a = null;
            try {
                if ( GuiUtil.answerIsYes(new String[] {
                    " ",
                    "Cancel instance billing for " +  paramsEc2.uid + " ? ",
                    " "
                    }, "Cancel instance billing"
                )) {  
                    s3a = new S3Access();
                    String oldUid = new String(paramsEc2.uid);
                    CancelBillingMessage m = new CancelBillingMessage(paramsEc2.uid);
                    s3a.putObject(ConstantsUM.BUCKET_NAME_INBOX, m.key, m, false);                    
                    updateParamsReBillingAuthorizationID(null);
                    updateDisplayedBillingID();
                    GuiUtil.info(new String[] {
                        "  ",
                        "Instance billing has been canceled for " + oldUid,
                        "  " },
                        "Instance billing canceled");                    
                }
            }
            catch(Exception xxx) { GuiUtil.exceptionMessage(xxx); }
            finally { if ( s3a != null ) s3a.close(); } 
        }
        else {
            GuiUtil.warning(new String[] {
                "There is no user ID/email address in your config file.",
                "Please send email stating that you want to cancel ",
                "billing authorization to ",
                " ",
                "        billing@norbl.com",
                " ",
                "Please include your name and if possible the email address ",
                "you specified when you authorized billing."
                },
                "User ID missing");            
        }
    }    
        
    public void editEc2Parameters() {
        try {
            final Ec2ParamEditor ed = new Ec2ParamEditor(PPEManager.this); //paramHt);
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ed.create();                
                }
            });

            while ( ed.isRunning ) {
                try { Thread.sleep(500L); }
                catch(InterruptedException ix) {}
            }

            Ec2AuthorizationIDParams eips = ed.getParams();

            if ( (eips != null) && eips.paramsOk() ) {

                    // Add the params the paramsHT
                eips.putInto(paramHt);

                saveParams();
                                 
                            // Setup aws wrangling
                setupAWSWrangling();            
            }
        }
        catch(Exception xxx) { GuiUtil.exceptionMessage(xxx); }
    }
    
    public void editAwsClientParameters() {
        awsClientConfigParams.editParams();
    }
    
    /** Writes the contents of paramHt to the config file and
     *  updates paramEc2.
     * @throws Exception 
     */
    public void saveParams() throws Exception {        
        paramHt.saveAndReload();        
        paramsEc2 = new ParamsEc2(paramHt);
    }
          
    public void listEbsVolumes() {               
                        
        java.awt.EventQueue.invokeLater(new Runnable() {            
            public void run() {                
                if ( ebsVolumeWrangler == null ) return;
                if ( ebsVolumeListFrame == null ) {                
                    ebsVolumeWrangler.retrieveVolumeInfo();
                    ebsVolumeListFrame = new EbsVolumeListFrame(ebsVolumeWrangler);
                    ebsVolumeListFrame.launch();
                }
                else {
                    ebsVolumeWrangler.retrieveVolumeInfo();
                    ebsVolumeListFrame.setVisible(true);
                }
                InProgressFrame.end();
            }
        });
    }
    
    public void updateEbsVolumeList() {                   
        if ( ebsVolumeListFrame == null ) listEbsVolumes();
        else if ( ebsVolumeWrangler == null ) return;
        else ebsVolumeWrangler.retrieveVolumeInfo();                    
    }
    
    public String getBillingIDForDisplay() {
        if ( paramsEc2.uid != null ) return(paramsEc2.uid);
        else return("You have not authorized instance billing.");
    }
    
    public void updateDisplayedBillingID() {
        if ( (networkManagerFrame != null) && 
             (networkManagerFrame instanceof NetworkManagerFrame) ) {
            ((NetworkManagerFrame) networkManagerFrame).setDisplayedBillingID(
                                                getBillingIDForDisplay());
        }
    }
    
          // ------------------------------------------------

    public void startNetworkInfoUpdater() {
        niUpdater = new NiUpdater();
        (new Thread(niUpdater)).start();
    }

    public void stopNetworkInfoUpdater() {
        niUpdater.keepUpdatingNetworkInfo = false;
    }

    class NiUpdater implements Runnable {

        boolean keepUpdatingNetworkInfo;

        public void run() {
            keepUpdatingNetworkInfo = true;           
            while ( keepUpdatingNetworkInfo ) {
               NiM.update(ec2w);        
               ebsVolumeWrangler.retrieveVolumeInfo();
               try { Thread.sleep(ConstantsEc2.NETWORK_INFO_UPDATE_INTERVAL); }
               catch(InterruptedException ix) {}
            }
        }
    }
}
