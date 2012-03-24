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

package ppe.gui;

import java.util.*;
import ppe.ec2.*;
import ppe.*;
import java.awt.event.*;
import javax.swing.*;
import nbl.utilj.*;
import ppe.gui.networkspec.AmiNameEditor;
import ppe.util.*;


/** A gui for launching and managing a network of Ec2 instances.
 *
 * @author Barnet Wagman
 */
abstract public class PPEManager implements ActionListener {

     private enum CmdType { global, network, instance; }

     public enum Cmd {
        createNetwork("Create network",CmdType.global),
        updateNetworkInfo("Update",CmdType.global),
        exit("Exit",CmdType.global),

        rebootInstances("Reboot instances",CmdType.network),
        terminateInstances("Terminate instances",CmdType.network),

        editEc2InstanceTypes("Ec2 instance types",CmdType.global),
        editAmiNames("AMIs",CmdType.global),
        editEc2Params("Ec2 parameters",CmdType.global),
        
        showAbout("About",CmdType.global);
                                       
        String title;
        CmdType type;
        Class serviceClass;        
        
        Cmd(String title,CmdType type) {
            this.title = title;
            this.type = type;
        }

        Cmd(String title,CmdType type,Class serviceClass) {
            this(title,type);
            this.serviceClass = serviceClass;
        }

        public String getTitle() { return(title); }
        public boolean isNetworkCmd() { return( CmdType.network.equals(type)); }
        public boolean isInstanceCmd() { return( CmdType.instance.equals(type)); }
        public boolean isGlobalCmd() { return( CmdType.global.equals(type)); }
        public Class getServiceClass() { return(serviceClass); }
    }


    protected ParamHt paramHt;
    public static ParamsEc2 paramsEc2;
    protected Ec2Wrangler ec2w;

    NetworkTableModel networkTableModel;
    InstanceTableManager instanceTableManager;

    NiUpdater niUpdater;

    NetworkManagerFrame frame;

    String guiFrameTitle;
    
    private boolean hasRequiredParams;
    
    boolean shutdownAppOnExit;
    
    List<PPEManagerExitListener> exitListeners;

    /**
     * 
     * @param argv
     * @param guiFrameTitle
     * @param shutdownAppOnExit If true closing the window or using 
     *      the 'exit' menu item will shutdown the app using
     *      <tt>System.exit(0)</tt>.  Otherwise the exit command just
     *      closes the window.
     * @throws Exception 
     */
    public PPEManager(String[] argv,String guiFrameTitle,
                      boolean shutdownAppOnExit) throws Exception {

        System.setProperty("org.apache.commons.logging.Log",
                           "ppe.NearNilLog");
        this.shutdownAppOnExit = shutdownAppOnExit;
        hasRequiredParams = false;
        SwingDefaults.setDefaults();

        this.guiFrameTitle = guiFrameTitle;
       
        paramHt = new ParamHt(argv);
        paramsEc2 = new ParamsEc2(paramHt);
        if ( !paramsEc2.hasRequiredParams() ) {           
               // Prompt the user for ec2 params.
            if ( !promptUserForEc2Params(argv) ) {
                System.exit(0);
//                return;
            }             
        }
        hasRequiredParams = true;
        ec2w = new Ec2Wrangler(paramsEc2);

        NiM.init();
        // ^ Order is important, NiM.init() must follow new Ec2Wrangler().

        specifyServices();

        networkTableModel = new NetworkTableModel();
        NiM.addStateChangeListener(networkTableModel);
        instanceTableManager = new InstanceTableManager();
        NiM.addStateChangeListener(instanceTableManager);

        startNetworkInfoUpdater();
    }

    /** Equivalent to <tt>PPEManager(argv,guiFrameTitle,true)</tt>
     * 
     * @param argv
     * @param guiFrameTitle
     * @throws Exception 
     */
    public PPEManager(String[] argv,String guiFrameTitle) 
        throws Exception {
        this(argv, guiFrameTitle, true);
    }
    
    abstract protected void specifyServices();
    abstract protected void createNetwork() throws Exception;
    abstract protected void rebootInstances(String networkID) throws Exception ;

    public void launchGui() {
        if ( !hasRequiredParams ) return;
        InProgressFrame.showInProgress();
        frame = new NetworkManagerFrame(this,guiFrameTitle);
        try {            
            java.awt.EventQueue.invokeAndWait(new Runnable() { public void run() {
                frame.startGui();
            }});
            InProgressFrame.end();
            NiM.update(ec2w);            
        }
        catch(Exception xxx) { {
            InProgressFrame.end();
            ExceptionHandler.gui(xxx);
        } }
    }

    public void updateParams() {
        try {
            paramHt.updateParams();
            paramsEc2 = new ParamsEc2(paramHt);
            if ( paramsEc2.hasRequiredParams() ) {               
                hasRequiredParams = true;
                if ( (ec2w != null) && (ec2w.ec2Client == null) )
                     ec2w.ec2Client.shutdown();
                ec2w = new Ec2Wrangler(paramsEc2);
            }
        }
        catch(Exception xx) { GuiUtil.exceptionMessage(xx); }
    }
    
    public NetworkTableModel getNetworkTableModel() {
        return(networkTableModel);
    }

    public void actionPerformed(ActionEvent ev) {

        ActionCommand ac = new ActionCommand(ev);
        Cmd cmd = Cmd.valueOf(ac.cmd);        

        switch(cmd) {
            case createNetwork:
                (new CreateNetworkOp()).start();
                break;
            case updateNetworkInfo:
                (new UpdateNetworkInfoOp()).start();
                break;            
            case rebootInstances:
                (new RebootInstancesOp(ac.networkID)).start();
                break;
            case terminateInstances:
                (new TerminateInstancesOp(ac.networkID)).start();
                break;                                   
            case editEc2InstanceTypes:
                (new EditEc2InstanceTypesOp()).start();
                break;
            case editAmiNames:
                (new EditAmiNamesOp()).start();
                break;    
            case editEc2Params:
                (new EditEc2ParamsOp()).start();
                break;
            case exit:
                (new ExitOp()).start();
                break;
            case showAbout:
                (new ShowAboutOp()).start();
                break;
            default:
        }
    }

        // ------------------------------------------------

        /** Operations triggered by actions should not be performed
         *  in the aws event dispatching thread, so we run them in
         *  their threads.
         *  
         */
    abstract class Op extends Thread {
        public void run() {
            try {                               
                op();
                NiM.update(ec2w);                
            }
            catch(Exception xxx) { ExceptionHandler.gui(xxx); }
        }
        abstract void op() throws Exception;
    }

    class CreateNetworkOp extends Op {
        void op() throws Exception {
            createNetwork();
        }
    }

    class RebootInstancesOp extends Op {
        String networkID;

        public RebootInstancesOp(String networkID) { 
            this.networkID = networkID;
        }

        void op() throws Exception {
            rebootInstances(networkID);
        }
    }

    class TerminateInstancesOp extends Op {
        String networkID;

        public TerminateInstancesOp(String networkID) { 
            this.networkID = networkID;
        }

        void op() throws Exception {
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
    }

    class UpdateNetworkInfoOp extends Op {
        void op() { NiM.update(ec2w); }
    }    

    class ExitOp extends Op {     
        public void op() { exit(false); }
    }

    class ShowAboutOp extends Op {

        public void op() {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    AboutGui ag = new AboutGui();
                    GuiUtil.centerOnScreen(ag);
                    SwingDefaults.setIcon(ag);
//                    AboutGui.setNBLIcon(ag);
                    ag.setVisible(true);
                }
            });
        }
    }
    
    class EditEc2InstanceTypesOp extends Op {
        
        public void op() {
            final InstanceTypeEditor ed = new InstanceTypeEditor();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {              
                    ed.create();
                }
            }); 
        }
    }
    
    class EditAmiNamesOp extends Op {
        
        public void op() {
            final AmiNameEditor ed = new AmiNameEditor();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {                    
                    ed.create();
                }
            }); 
        }
    }
    
    class EditEc2ParamsOp extends Op {
        public void op() {
            final Ec2ParamEditor ed = new Ec2ParamEditor(PPEManager.this); //paramHt);
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ed.create();
                }
            });
        }
    }

        // ------------------------------------------------

        /**
         *
         * @param recreateWindowOnCancel if true, exit was triggered
         *  by a window closing event.
         */
    public void exit(boolean recreateWindowOnCancel) {
        if ( shutdownAppOnExit ) System.exit(0);
        else if ( frame != null ) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {          
                    frame.disposeOfWindow();
                }
            });
            
            if ( exitListeners != null ) {
                for ( PPEManagerExitListener xl : exitListeners ) {
                    xl.ppeManagerExiting();
                }
            }
        }
    }    
    
    public void terminateAllInstances(NetworkInfo ni) {
        ec2w.terminateInstances(ni);               
        NiM.update(ec2w);
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
               try { Thread.sleep(ppe.ec2.Constants.NETWORK_INFO_UPDATE_INTERVAL); }
               catch(InterruptedException ix) {}
            }
        }
    }

        // --------------------------------------------

    enum PingFailedResponse {
        wait("Continue to wait"),
        proceed("Proceed"),
        terminate("Terminate and proceed");

        String title;
        PingFailedResponse(String title) {
            this.title = title;
        }
    }

        /**
         *
         * @param networkID
         * @return false if there are NO usable instances.
         */
    protected boolean pingNetwork(String networkID) {

        for (;;) {
            try {
                Pinger pinger = Pinger.pingNetwork(networkID,
                                ppe.Constants.PING_NETWORK_MAX_MILLIS);
                if ( pinger.allSucceeded() ) return(true);
                else {
                    PingFailedResponse r = getPingFailedResponse(pinger);
                    if ( PingFailedResponse.proceed.equals(r) ) {
                        return( pingFailedRetag(pinger) );
                    }
                    else if ( PingFailedResponse.terminate.equals(r) ) {
                        return( terminatePingFailures(pinger) );
                    }
                    // else continue waiting
                }
            }
            catch(NoSuchNetworkException nx) {
                ExceptionHandler.gui(nx);
                    GuiUtil.warning(new String[] {
                        "   Ompi may not have been configured"
                        },
                        "Warning");
            }
        }
    }

    PingFailedResponse getPingFailedResponse(Pinger pinger) {

        Object[] options = { PingFailedResponse.wait.title,
                             PingFailedResponse.terminate.title,
                             PingFailedResponse.proceed.title                             
                           };

        int rn = JOptionPane.showOptionDialog(null,
                                              buildPingFailedMessage(pinger),
                                              "Unresponsive instance",
                                              JOptionPane.YES_NO_CANCEL_OPTION,
                                              JOptionPane.WARNING_MESSAGE,
                                              null,
                                              options,
                                              options[0]);


        String r = (String) options[rn];
        if ( PingFailedResponse.wait.title.equals((r)) )
            return(PingFailedResponse.wait);
        else if (PingFailedResponse.proceed.title.equals((r)))
            return(PingFailedResponse.proceed);
        else if (PingFailedResponse.terminate.title.equals((r)))
            return(PingFailedResponse.terminate);
        else throw new RuntimeException("Undefined reponse=" + rn + " " + r);

    }

    String buildPingFailedMessage(Pinger pinger) {

        StringBuilder s = new StringBuilder("<html>");

        s.append("The following instances are <i>not</i> responding: " +
                  "<blockquote>");
        s.append( pinger.failureNamesToHtmlLines() );
        s.append("</blockquote>");
        s.append("<br>You have three choices.<ul>");
        s.append("<li>Continue to wait for the instances to respond. " +
                 " if there's no response for 10 minutes, you'll be " +
                 " given this choice again.</li>");
        s.append("<li>Terminate the unresponsive instances and proceed.</li>");
        s.append("<li>Proceed but do not terminate the unresponsive " +
                  " instances. They will be " +
                   "omitted from the network but they will continue to " +
                   "run.</li>");
        s.append("</ul><br>");
        s.append("</html>");

        return(s.toString());
    }

        /** Instances that failed to respond to ssh ping are
         *  retagged as non network members. If one of these is the
         *  master, a new master is created.
         * @param pinger
         * @return false if there are NO usable instances
         */
    private boolean pingFailedRetag(Pinger pinger) {

        if ( (pinger.failures == null) || (pinger.failures.size() < 1) ) {
            if ( pinger.successes != null )
                return( pinger.successes.size() > 0 );
            else return(false);
        }

        boolean masterFailed = false;

        for ( InstanceStatus f : pinger.failures ) {
            if ( f.isMaster() ) masterFailed = true;
            String ID = f.instance.getInstanceId();
            ec2w.setTags(ID,"nil","nil - unresponsive instance",
                         NodeType.slave);
        }

        if ( masterFailed ) { // Retag one responsive slave to master.

            if ( (pinger.successes == null) || (pinger.successes.size() < 1) ) {
                ExceptionHandler.display(new RuntimeException("There are " +
                        " no usable instances."));
                return(false);
            }

            InstanceStatus m = pinger.successes.get(0);
            ec2w.setTags(m.instance.getInstanceId(),
                         m.getNetworkID(),
                         m.getNetworkName(),
                         NodeType.master);
        }

        return(true);
    }

        /** Terminates instances that did not respond to ping.
         *  Before terminating, calls {@link #pingFailedRetag(ppe.util.Pinger)}
         *  to ensure that we have a master.
         *
         * @param pinger
         * @return false if there are no usable instancers.
         * 
         */
    private boolean terminatePingFailures(Pinger pinger) {

        pingFailedRetag(pinger);

        if ( (pinger.failures == null) || (pinger.failures.size() < 1) ) {
            if ( pinger.successes != null )
                return(pinger.successes.size() > 0 );
            else return(false);
        }

        List<String> failedIDs = new ArrayList<String>();
        for ( InstanceStatus f : pinger.failures ) {
            failedIDs.add(f.instance.getInstanceId());            
        }

        ec2w.terminateInstances(failedIDs);

        GuiUtil.warning(buildTermMessage(pinger.failures),
                                         "Terminated instances");

        return(true);
    }

    private String[] buildTermMessage(List<InstanceStatus> failures) {

        List<String> lines = new ArrayList<String>();
        lines.add("The following instances were terminated:");
        lines.add(" ");

        for ( InstanceStatus f : failures ) {
            lines.add("    " + f.getPublicDnsName());
        }

        return( lines.toArray(new String[ lines.size() ]) );
    }

    private int getContinuePortForwardingReply() {

        int nOpen = 0;
        String ntNames = "";
        List<NetworkInfo> nis = NiM.getNetworks();
        for ( NetworkInfo ni : nis ) {
            if ( ni.isRunning() ) {
                ++nOpen;
                ntNames += ni.getNetworkName() + " ";
            }
        }

        if ( nOpen < 1 ) return(JOptionPane.NO_OPTION);

        String m;
        if ( nOpen == 1 )
            m = "Network " + ntNames.trim() + " is running.";
        else m = "Networks " + ntNames.trim() + " are running.";

        return(  GuiUtil.answerIsYesNoCancel( new String[] {
                     "<html><b>" + m + "</b><br><br>" +
                            "To continue using Rmpi and npRmpi, port forwarding must not be stopped.<br><br>"  +
                            "<b>Continue port forwarding?</b><br><br>" +
                             "<blockquote>" +
                             "If you select 'yes', port forwarding will continue in a background " +
                             "process. You will have to explicitly kill the process to stop it.<br><br>" +
                             "If you select 'no', port forwarding will be stopped.<br><br>" +
                             "Choose 'cancel' to leave this window open." +
                             "</blockquote><br>" +
                      "</html>"
//                    m,
//                    " ",
//                    "To continue using Rmpi and npRmpi, port forwarding must not be stopped.",
//                    " ",
//                    "Continue port forwarding?",
//                    " ",
//                    "If you select 'yes', port forwarding will continue in a background",
//                    "process. You will have to explicitly kill the process to stop it.",
//                    " ",
//                    "If you select 'no', port forwarding will be stopped.",
//                    " ",
//                    "Choose 'cancel' to leave this window open."
                    },
                    "Continue port forwarding?")
               );
    }
    
        // -------------------------------------------------------------------
    
    /** Blocks until the user is finished.
     * 
     */
     boolean promptUserForEc2Params(String[] argv) {
         try {
             GuiMetrics.init();

             final Ec2ParamEditor ed = new Ec2ParamEditor(this); // paramHt);
             java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ed.create();
                }});

             while (ed.isRunning ) {
                 try { Thread.sleep(500L); } catch(InterruptedException ix) {}
             }
             paramHt = new ParamHt(argv);
             paramsEc2 = new ParamsEc2(paramHt);                  
             if ( paramsEc2.hasRequiredParams() ) return(true);
             else {
                 boolean quitNow = GuiUtil.answerIsYes(
                     new String[] {
                         "The required Ec2 parameters are not available - the ppe network",
                         "manager cannot run without them.",
                         " ",
                         "You can either try to enter them again or quit and ",
                         "create the config file " + ParamHt.getDefaultConfigFilePath() +
                         " with a text editor.",
                         " ",
                         "Quit now?"
                     },
                     "Ec2 parameters missing");
                 if ( quitNow ) return(false);
                 else return( promptUserForEc2Params(argv));
             }
         }
         catch(Exception x) {
             GuiUtil.exceptionMessage(x);
             return(false);
         }
     }     
     
     public void addExitListener(PPEManagerExitListener xl) {
         
         if ( exitListeners == null )
             exitListeners = new ArrayList<PPEManagerExitListener>();
         
         exitListeners.add(xl);
     }
}
