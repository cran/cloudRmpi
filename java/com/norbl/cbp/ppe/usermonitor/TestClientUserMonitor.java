/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;
import com.norbl.util.aws.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/** Standalone app to test the UserMonitor.
 *
 * @author Barnet Wagman
 */
public class TestClientUserMonitor {

    public static void main(String[] argv) throws Exception {
        
        TestClientUserMonitor tc = new TestClientUserMonitor();
        tc.go();        
    }
    
    public void go() throws Exception {
        
        ParamHtPPE paramHt = new ParamHtPPE(new String[] {});
        
        
        String hostname = MonitorHostname.getHostNameFromS3();
        System.out.println("UserMonitor host=" + hostname);
        
        for ( int i = 0; i < 20; i++ ) {
        
            Socket socket = new Socket(hostname,ConstantsUM.JPORT_DEFAULT);

            ReadWriteConnection con =  
                    new ReadWriteConnection(socket,ReadWriteConnection.HostType.client);
              
            Message m = new Message.TestMessage("Hello UserMonitor " +
                                    TimeUtil.toDateTimeString(System.currentTimeMillis()));
            con.writeMessage(m);
            Message r = (Message) con.readMessage();
            System.out.println("REPLY: " + r.toString());
            
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }        
    }
}
