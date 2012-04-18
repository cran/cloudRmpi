/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.aws.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.http.*;
import java.net.*;
import org.apache.http.protocol.*;

/** App run on web accessible host to handle user authorization,
 *  record user usage and submit bills. This app runs two 'servers',
 *  one that accepts commands from the user (requesting billing authorization)
 *  and the the other handling the response from AWS to the authorization
 *  dialog.  The latter runs runs on port 80, which means that this app 
 *  must run as root (or in a chroot jail).
 *
 * @author moi
 */
public class UserMonitor {
            
    public static final String POST_AUTH_URI_FLAG = "postauth";
    
    int jPort;
    int hPort;
    ParamHt paramHt;
    ParamsEc2 paramEc2;
    UserDb userDb;
    UserStatus userStatus;
    S3Access s3Access;    
    SDBAccess sdbAccess;
    
    String postAuthorizationUrl;
    
    MessageServer messageServer;
    
    public static boolean verbose;
    
    /** Parameters taken from the config file:
     *  <ul>
     *  <li>awsUserID</li>
     *  <li>awsAccessKey</li>
     *  <li>awsSecretKey</li>    
     *  <li>portJ</li>
     *  <li>portH</li>
     *  </ul>     
     * 
     * @param argv
     * @throws Exception 
     */
    public UserMonitor(String[] argv) throws Exception {        
        
        paramHt = new ParamHtPPE(argv);
        paramEc2 = new ParamsEc2(paramHt);
        sdbAccess = new SDBAccess(paramEc2);
        this.userDb = new UserDb(sdbAccess);
        s3Access = new S3Access(paramEc2);   
        userStatus = new UserStatus(s3Access);
        
        verbose = ArgvUtil.getVal(argv,"verbose") != null;
        
        setupServerURL();
        
        String pj = paramHt.get("jPort");
        if ( pj != null ) jPort = Integer.parseInt(pj);
        else jPort = ConstantsUM.JPORT_DEFAULT;
        
        String ph = paramHt.get("hPort");
        if ( ph != null ) hPort = Integer.parseInt(ph);
        else hPort = ConstantsUM.HPORT_DEFAULT;
        
            // Start the http server that will called by AWS
            // after authorization
        HttpRequestHandler postAuthorizationHandler =
                        new PostAuthorizationHandler(userDb);
        
        Thread httpReqThread = 
            new SimpleHttpServer.ReqListenerThread(postAuthorizationHandler, hPort);
        httpReqThread.setDaemon(false);
        httpReqThread.start();
       
            // Start the server to handle messages from java apps
        messageServer = new MessageServer();
        Thread mt = new Thread(messageServer);
        mt.setDaemon(false);
        mt.start();
    }    
    
    /** Records the public dns in s3 ad creates the
     *  postAuthorizationUrl: this is http://<domain> with NO trailing
     *  '/' and no local path.
     * 
     * @throws Exception 
     */
    private void setupServerURL() throws Exception {
        
        String hostname = MonitorHostname.getThisHostsPublicName();
        MonitorHostname.setHostNameOnS3(s3Access,hostname);
        
        postAuthorizationUrl = "http://" + hostname;        
    }
    
   /** Server side of communications between two java apps.  There is one
    *  server that can respond to multiple clients.  Clients initiate
    *  contact.  Every message from a client elicits a response message
    *  from the server.  These response messages are separate from the 
    *  acks used to acknowledge receiving a message.
    */ 
    class MessageServer implements Runnable {
        
        boolean keepRunning;
        
        public void run() {        
            try { 
                keepRunning = true;
                ServerSocket serverSocket = new ServerSocket(jPort);
                System.out.println("MessageServer listening on port " + jPort);
                while (keepRunning) {
                    try {
                    Socket s = serverSocket.accept();
                    UserMonitorMessageHandler h = 
                        new UserMonitorMessageHandler(paramEc2, 
                                                      userDb, 
                                                      userStatus,
                                                      s,
                                                      postAuthorizationUrl);
                    (new Thread(h)).start();
                    }
                    catch(Exception xxx) {}
                    // ^ If an error occurs in accept(), we ignore it silently.
                }                
            }
            catch(Exception iox) {
                System.err.println("Terminal exception: " + iox);
                System.exit(0);
            }
        }
    }
    
        // ...................................................
    
    public static void main(String[] argv) throws Exception {
        
        System.setProperty("org.apache.commons.logging.Log",
                           "com.norbl.util.NearNilLog");
        
        UserMonitor um = new UserMonitor(argv);
    }
}
