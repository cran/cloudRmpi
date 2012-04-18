/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.cbp.ppe.*;
import com.norbl.util.gui.*;
import com.norbl.util.aws.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/** Sends authorization request to the {@link UserMonitor}.  The reply
 *  from the server contains a url for the AWS authorization web page
 *  dialog.  This classes launches browser to show that url.
 *
 * @author Barnet Wagman
 */
public class AuthorizationClient implements ActionListener {   
    
    PPEManager ppeManager;
    
    public AuthorizationClient(PPEManager ppeManger) {
        this.ppeManager = ppeManger;
    }
    
    AuthorizationFrame f;
    
    public void runAuthorizationDialog() {
        
            // Get email address from user       
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                f = new AuthorizationFrame(AuthorizationClient.this);
            }
        });        
    }
    
    public void actionPerformed(ActionEvent ev) {
    
        String c = ev.getActionCommand();
        if ( c.equals("cancel") ) {
            if ( f != null ) f.dispose();
        }
        else if ( c.equals("continue") ) {
            String ema = f.getEmailAddress();
            if ( isValueEmailAddress(ema) ) {
                doAuthorization(ema);
                f.dispose();
            }
            else {
                if ( !GuiUtil.answerIsYes(new String[] {
                    "'" + ema + "' is not a valid email address.",
                    " ",
                    "Try again?" },
                    "Invalid email address") ) {
                    if ( f != null ) f.dispose();
                };
            }
        }    
    }
    
    private void doAuthorization(String ema) {
        try {
                // Send authorization message
            String hostname = MonitorHostname.getHostNameFromS3();
            if ( hostname == null )
                throw new Exception("Unable to get the hostname " +
                   "of the ppe user monitor server; " +
                   "this implies an error accessing S3 so " +
                   "check you connection to the internet."); 
            Socket socket = null;
            try {
                socket = new Socket(hostname,ConstantsUM.JPORT_DEFAULT);
            }
            catch(Exception xxx) {
                GuiUtil.warning(new String[] {
                    "Exception: " + xxx.getMessage(),
                    "This exception occurred while attempting to connect to our",
                    "server on port " + Integer.toString(ConstantsUM.JPORT_DEFAULT) +
                        "(our server is running on an ec2 instance).", 
                    "If you are accessing the internet via a proxy server,",
                    "perhaps it's blocking access."
                    },
                    "Exception"
                    );
            }
            
            
            ReadWriteConnection con =  
                new ReadWriteConnection(socket,ReadWriteConnection.HostType.client);
            con.writeMessage(new Message.AddUser(ema));
            
            Message r = (Message) con.readMessage();
            
            if ( r instanceof Message.NewUserUrl ) {
                
                    // Receiving a NewUserUrl message means that
                    // the user has been recorded in UserDb abd UseStatus
                    // with status pending. The user is now allowed
                    // to user instances (whether or not he actually
                    // completed authoriztation, so update the params
                    // to reflect this.
                ppeManager.updateParamsReBillingAuthorizationID(ema);                
                ppeManager.updateDisplayedBillingID();
                
                    // Launch browser with the returned url
                Message.NewUserUrl um = (Message.NewUserUrl) r;        
                                    
                java.awt.Desktop.getDesktop().browse(new URI(um.url));                                                   
            }
            else if ( r instanceof Message.Error ) {
                GuiUtil.exceptionMessage(((Message.Error) r).exception);
            }
            else {
                GuiUtil.info(new String[] { 
                    "An error of unknown type occurred.",
                    " ",
                    "You may want to try the authorization again."},
                    "Error");
            }        
        }
        catch(Exception xxx) { GuiUtil.exceptionMessageOnly(xxx); }       
    }
    
    private boolean isValueEmailAddress(String ema) {
        if ( ema == null ) return(false);
        if ( ema.length() < 4 ) return(false);
        return( ema.contains("@") );
    }
    
    public void windowClosed() {
        if ( f != null ) f.dispose();        
    }        
    
//    private void updateParams(String ema) throws Exception {        
//        ppeManager.paramHt.put(ParamsEc2.ParamName.uid.toString(),ema);       
//        ppeManager.paramHt.saveAndReload(false);
//        ppeManager.paramsEc2 = new ParamsEc2(ppeManager.paramHt);
//    }
}
