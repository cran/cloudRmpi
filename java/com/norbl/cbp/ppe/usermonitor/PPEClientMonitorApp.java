/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.aws.*;
import java.net.*;
import java.io.*;
import com.norbl.util.*;

/** This app is run on an ec2 instance in a ppe-ompi network. It should
 *  be launched at boot time.<p>
 * 
 *  Initially, this app checks whether the instance is authorized
 *  to use a billable instance.  If not, access to ompi is 
 *  disabled.  If authorized, the app runs forever, logging
 *  usage.
 *
 * @author Barnet Wagman
 */
public class PPEClientMonitorApp {
    
    /** One hour plus 100 millisecs.
     * 
     */
    static final long REPORT_INCREMENT = (1000L * 60L * 60L) + 100L;
    static final long NAP_TIME = 1000L * 60L;

    private String uid,peid;
    
    private boolean verbose;
    private long reportIncrement;
    
    public PPEClientMonitorApp(String[] argv) {
        
        String v = ArgvUtil.getVal(argv, "verbose");
        verbose = (v != null);
        
        int ri = ArgvUtil.getIntVal(argv, "ri", -1);
        if ( ri <= 0 ) reportIncrement = REPORT_INCREMENT;
        else reportIncrement = 1000L * ((long) ri);       
        showV("reportIncrement=" + reportIncrement);
    }
    
    public void go() {
        
        if ( !checkAuthorization() ) {
            showV("Authorization failed, disabling");
            disable();
            return;
        }
        else {            
            if ( uid == null ) {
                // Failed to get user data which may a bug, so let
                // the user go unmonitored.
                System.err.println("PPEClientMonitorApp.run() null peod -> " +
                                   "possible BUG.");
                System.exit(0);
            }
            
            reportUsage();
        }
    }
    
    boolean checkAuthorization() {   
        try {
            uid = EC2Utils.getInstanceUserdata();
            if ( uid == null ) return(false);
            else {                
                showV("userData=" + uid);                      
                if ( uid.equals(ConstantsUM.NIL_USER_DATA) ) return(false);
                else {
                    peid = S3Key.createPeidKey(uid);
                    showV("peid=" + peid);
                    return(true);
                }
            }
        }
        catch(Exception iox ) {
            // Failed to get the user peid.  Since this may be
            // a bug, return true.
            return(true);
        }
    }
    
    private static void disable() {
        System.out.println("Disabling ...");
        
        renameIfExtant(new File("/home/ec2-user"));
        renameIfExtant(new File("/home/apps"));        
    }
    
    private static void renameIfExtant(File dir) {
        if ( !dir.exists() ) return;
        if ( !dir.isDirectory() ) return;
        File f = new File(dir,"openmpi");
        if ( f.exists() ) f.renameTo(new File(dir,"xxxopenmpi"));
    }
    
    void reportUsage() {
        try {
            String monitorHost = MonitorHostname.getHostNameFromS3();
           
            ReadWriteConnection con = null;    
            Socket socket = null;

            Message m = new Message.BillableUsage(uid);

            long lastReportTime = 0L;
            for (;;) {
                try {
                    long curTime = System.currentTimeMillis();
                    if ( curTime >= (lastReportTime + reportIncrement) ) {
                        try {
                            socket = new Socket(monitorHost,ConstantsUM.JPORT_DEFAULT);
                            if ( verbose ) System.out.println("Writing bill message.");
                            con = new ReadWriteConnection(socket, 
                                            ReadWriteConnection.HostType.client);
                            con.writeMessage(m);   
                            if ( verbose ) System.out.println("... wrote bill message.");
                            Message r = (Message) con.readMessage();
                            if ( verbose ) System.out.println("... got reply=" + r);
                            con.close();
                            socket.close();
                            // ^ This looks odd but it is correct. The server
                            //   closes the socket after handling one message,
                            //   so we must reconnect for each write.  This looks 
                            //   inefficient but its not, because messages are sent
                            //   hourly.  This is preferable to having the server
                            //   keep an arbitrary number of sockets (and threads)
                            //   alive indefintely.
                            if ( verbose ) System.out.println("___ done");
                        }
                        catch(Exception xxx) {
                            try {
                                if ( con != null ) con.close();
                                if ( socket != null ) socket.close();
                            }
                            catch(Exception zzz) { System.err.println(zzz); }
                            System.err.println(xxx.getMessage());
                        }                        
                        lastReportTime = curTime;                        
                    }
                    else {
                        try { Thread.sleep(NAP_TIME); }
                        catch(InterruptedException ix) {}
                    }
                }
                catch(Exception xxx) {
                    System.err.println(xxx);
                    // Ignore message in the loop.
                }
            }
        }
        catch(Exception xxx) {
            // An error indicates a bug (including server not running, so
            // quit let the user proceed.
            System.err.println(xxx);
            System.exit(0);
        }
    }
    
    void showV(String m) {
        if ( verbose ) System.out.println(m);
    }
    
    public static void main(String[] argv) {
        
        PPEClientMonitorApp app = new PPEClientMonitorApp(argv);
        app.go();
    }
}
