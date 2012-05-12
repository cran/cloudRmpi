/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of cloudRmpi.

    cloudRmpi is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    cloudRmpi is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with cloudRmpi.  If not, see <http://www.gnu.org/licenses/>.
 */

package cloudrmpi;

import com.amazonaws.services.ec2.model.*;
import java.net.*;
import com.norbl.cbp.ppe.*;
import com.norbl.cbp.ppe.gui.networkspec.*;
import com.norbl.util.*;
import com.norbl.cbp.ppe.ompi.*;
import com.norbl.util.gui.*;
import rreval.*;
import java.util.*;
import java.io.*;

/** <tt>ROmpiPPEManage</tt> handles commands from R.  Otherwise
 *  is identical (except for some params) to {@link OmpiPPEManager}.
 *
 * @author moi
 */
public class ROmpiPPEManager extends OmpiPPEManager {
    
    public static final Class R_AMI_CHOICE_CLASS = AmiChoiceU.class;
    public static final Class R_NSG_FRAME_CLASS = NSGFrameU.class;
    
    public static String PORT_LABEL = "ppeManagerPort";
    public static int PORT_DEFAULT = 4461;
    public static boolean VERBOSE_DEFAULT = true; // false
    
    int cmdPort;
    ServerSocket serverSocket;
    
    boolean keepRunning;    
    List<CmdHandler> cmdHandlers = new ArrayList<CmdHandler>();
    
    public ROmpiPPEManager(String[] argv, 
                           String amiGroup,
                           Region ec2Region) throws Exception {
        super(argv,amiGroup,ec2Region);
        cmdPort = ArgvUtil.getIntVal(argv, PORT_LABEL, PORT_DEFAULT);  
        String verb = ArgvUtil.getVal(argv,"verbose");
        if ( verb == null ) Verbose.verbose = VERBOSE_DEFAULT;
        else if ( verb.equals("true") ) Verbose.verbose = true;
        else if ( verb.equals("false") ) Verbose.verbose = false;
        else Verbose.verbose = VERBOSE_DEFAULT;
    }
    
    /**
     * 
     * @param networkName
     * @return A fully specified network spec or null if the user cancels.
     */
    protected NetworkSpec getFullySpecifiedNetworkSpec(String networkName) {
        try {
            NetworkSpecGui nsg = new NetworkSpecGui(this,
                                                    R_AMI_CHOICE_CLASS,
                                                    R_NSG_FRAME_CLASS,
                                                    networkName);
            if ( nsg.fillSpec() ) return(nsg.choicesToNetworkSpec());
            else return(null);        
        }
        catch(Exception xxx) {
            GuiUtil.exceptionMessage(xxx);
            return(null);
        }
    }     
    
    protected String getAboutAppTitle() { return("cloudRmpi"); }
    protected String getAboutAppVersion() { 
        return("<html>" + 
        "Version 1.1<br><br><br> &copy; 2012, Barnet Wagman<br>Northbranchlogic, Inc." +
                "<html>"
              );
    }
    
    public void startCmdHandler() {
        socketServer();
    }
    
        // ------------- Cmd handler -------------------------------
    
    private enum Cmd {
        getNetworkNames,
        getNetworkIDs,
        getNetworkStateDescription,
        getMasterNodeURL,
        getNActiveInstances,
        getPemFile,
        terminateNetwork,
        disconnect,
        shutdown
    }

    private enum Arg {
        networkID,
        networkName
    }
       
        // ----------------------------------------------------
    
    void socketServer() {
        try {
            serverSocket = new ServerSocket(cmdPort);
            keepRunning = true;
            while ( keepRunning ) {
                Socket s = serverSocket.accept();   
                CmdHandler h = new CmdHandler(s);
                cmdHandlers.add(h);
                (new Thread(h)).start();
                Verbose.show("Launched new CmdHandler");
            }
        }
        catch(IOException iox) {
            System.out.println(StringUtil.toString(iox));
        }
    }    
    
        // ---------------------------------------------------
    
    class CmdHandler extends MessageWrangler implements Runnable {
               
        Socket socket;
        boolean keepRunning;
        
        CmdHandler(Socket s) {
            socket = s;
        }
        
        public void run() {
            try {
                conR = new ConnectionR(socket);
            }
            catch(Exception x) { 
                System.out.println(StringUtil.toString(x));
                System.exit(0);
            }
                                   
            CmdHandler.this.keepRunning = true;
            Verbose.show("... ready to start receiving messages.");
            
            while ( CmdHandler.this.keepRunning ) {        
            
                Message m = null;
                try {
                    m = conR.readMessage();
                    Verbose.show("Received message=" + m);
                }
                catch(Exception xx) {
                    if ( xx instanceof RReaderEOFException ) {
                        Verbose.show("Closing CmdHandler",xx);
                        closeCmdHandler();
                        return;
                    } 
                    // Else ignore, ack has been sent.
                } 
                
                try {
                
                    if ( m != null ) {
                            // NOTE that ROmpiPPEManager uses Message from
                            // rreval but does NOT check the message type
                            // because it only receives messages of one type.
                        AppCmd ac = new AppCmd(m.obj);

                        Verbose.show("ClientCmdHandler app cmd=" + ac.toString());

                        Cmd c = Cmd.valueOf(ac.getCmdName());   
                    
                        switch(c) {
                            case getNetworkNames:
                                sendMessageToR(getNetworkNames());
                                break;
                            case getNetworkIDs:
                                sendMessageToR(getNetworkIDs());
                                break;
                            case getNetworkStateDescription:
                                sendMessageToR(getNetworkStateDescription(ac));
                                break;
                            case getMasterNodeURL:
                                sendMessageToR(getMasterNodeURL(ac));
                                break;
                            case getNActiveInstances:
                                sendMessageToR(getNActiveInstances(ac));
                                break;
                            case getPemFile:
                                sendMessageToR(getPemFile());
                                break;
                            case terminateNetwork:
                                sendMessageToR(terminateNetwork(ac));
                                break;
                            case disconnect:
                                sendMessageToR("Disconnecting from ROmpiPPEManager");
                                closeCmdHandler();
                                break;
                            case shutdown:
                                sendMessageToR("Shutting down ROmpiPPEManager");
                                ROmpiPPEManager.this.shutdown();
                                break;
                            default:
                        }                    
                    }
                }
                catch(Exception x) {
                   Verbose.show("ClientCmdHandler.handleCmd(): ",x);
                    sendErrMToR("ClientCmdHandler.handleCmd()",x);
                }
            }
        }
        
        public void closeCmdHandler() {           
            CmdHandler.this.keepRunning = false;
            if (conR != null) {
                conR.close();
                conR = null;
            }
        }
    }
    
    void shutdown() {
        Verbose.show("Shutting down ROmpiPPEManager");
        try {
            keepRunning = false;
            serverSocket.close();

            for (CmdHandler h : cmdHandlers ) {
                h.closeCmdHandler();
            }
        }
        catch(Exception x) { System.err.println(StringUtil.toString(x)); }
        finally {
            System.exit(0);
        }
    }
   
    /** When the {@link OmpiPPEManager} window is closed, this app is
     *  shutdown completely.
     * 
     */
    public void ppeManagerExiting() {
        Verbose.show("The ompi-ppe manager has been closed.");
        ROmpiPPEManager.this.shutdown();
    }        
    
        // ---------------------------------------------
    
    /**
     * 
     * @return network names in a space delimited string.
     */
    String getNetworkNames() {
        
        List<NetworkInfo> nis = NiM.getNetworks();
        if ( nis.size() < 1 ) return("");
        
        StringBuilder s = new StringBuilder();
        for ( NetworkInfo ni : nis ) {
            s.append(ni.getNetworkName());
            s.append(" ");
        }
        
        return( s.toString().trim() );
    }
    
    /**
     * 
     * @return network names in a space delimited string.
     */
    String getNetworkIDs() {
        
        List<NetworkInfo> nis = NiM.getNetworks();
        if ( nis.size() < 1 ) return("");
        
        StringBuilder s = new StringBuilder();
        for ( NetworkInfo ni : nis ) {
            s.append(ni.getNetworkID());
            s.append(" ");
        }
        
        return( s.toString().trim() );
    }
    
    NetworkInfo getNi(AppCmd cmd) {
        String nID = cmd.getVal(Arg.networkID.toString());
        if ( (nID!= null) && (nID.length() > 0) ) {
            NetworkInfo ni = NiM.getForID(nID);
            if ( ni != null ) return(ni);
        }
        String nNm = cmd.getVal(Arg.networkName.toString());
        if ( (nNm != null) && (nNm.length() > 0) ) {
            NetworkInfo ni = NiM.getForName(nNm);
            if ( ni != null ) return(ni);
        }
        return(null);
    }
    
    
    String getNetworkStateDescription(AppCmd cmd) {        
        NetworkInfo ni = getNi(cmd);
        if ( ni != null ) return(ni.getStateDescription());
        else return("");
    }
    
    String getMasterNodeURL(AppCmd cmd) {
        NetworkInfo ni = getNi(cmd);
        if ( ni != null ) return(ni.getMastersPublicDnsName());
        else return("");
    }
    
    String getNActiveInstances(AppCmd cmd) {
        NetworkInfo ni = getNi(cmd);
        if ( ni != null ) 
            return(Integer.toString(ni.getNActiveInstances()));
        else return("0");
    }
    
    String getPemFile() {
        return(paramsEc2.rsaKeyPairFile.getPath());
    }
    
    String terminateNetwork(AppCmd cmd) {
        NetworkInfo ni = getNi(cmd);
        if ( ni == null ) {
            String networkName = cmd.getVal(Arg.networkName.toString());
            String networkID = cmd.getVal(Arg.networkID.toString());
            String s = "Unable to find network";
            if ( networkName != null ) s += " " + networkName;
            if ( networkID != null ) s += " " + networkID;
            return(s);
        }
        else {
            terminateInstances(ni.getNetworkID());
            return("Terminating all ec2 instances in network " + 
                    ni.getNetworkName());
        }
    }
    
     protected String getAmiWebpageUrl() {
        return("https://s3.amazonaws.com/norbl/Machine_images_cloudrmpi.html");
    }
     
    protected String getManualWebpageUrl() {
        return("http://norbl.com/cloudrmpi/cloudRmpi_Manual.html");
    } 
}
