/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.cbp.ppe.*;
import java.net.*;
import com.norbl.util.aws.*;
import com.norbl.util.*;

/** Responds to messages from network manager apps and instances
 *  in ppe networks. This class handles and responds to one message,
 *  replies and quits, shutting its connection.
 *
 * @author Barnet Wagman
 */
public class UserMonitorMessageHandler implements Runnable {
    
    public static int GLOBAL_AMOUNT_LIMIT_DEFAULT = 10000;
    public static int CREDIT_LIMIT_DEFAULT = 10000;        
    
    public enum MessageType { addUser }
    
    Socket socket;
    ParamsEc2 paramEc2;
    UserDb userDb;   
    UserStatus userStatus;
    ReadWriteConnection con;
    
    String postAuthorizationUrl;
    
    public UserMonitorMessageHandler(ParamsEc2 paramEc2,
                          UserDb userDb,  
                          UserStatus userStatus,
                          Socket socket,
                          String postAuthorizationUrl) {
        this.paramEc2 = paramEc2;
        this.socket = socket;
        this.userDb = userDb;       
        this.userStatus = userStatus;
        this.postAuthorizationUrl = postAuthorizationUrl;       
    }
    
    /** This method reads and replies to one message, then closes it's
     *  connnections and quits.
     */
    public void run() {
        
        try {
            con = new ReadWriteConnection(socket, 
                                          ReadWriteConnection.HostType.server);
            
            Object x = con.readMessage();
            if ( UserMonitor.verbose ) 
                System.out.println("Read message type " + x.getClass().getName());
            if ( !(x instanceof Message) ) {
                throw new BadMessageException(
                    "MessageHandler received non-message " + x);            
            }
            else  {
                if ( UserMonitor.verbose ) System.out.println("  " + x.toString());
                try {
                    Message reply = null;
                    if ( x instanceof Message.AddUser ) {
                        if ( UserMonitor.verbose ) System.out.println("   -> AddUser");
                        reply = addUser((Message.AddUser) x);                                       
                    }
                    else if ( x instanceof Message.BillableUsage ) {
                       if ( UserMonitor.verbose ) System.out.println("   -> BillableUsage"); 
                       reply = recordBillableUsage((Message.BillableUsage) x);
                    }
                    else if ( x instanceof Message.TestMessage ) {
                       if ( UserMonitor.verbose ) System.out.println("   -> Test"); 
                       reply = new Message.TestMessage("Echo of " + 
                                        ((Message.TestMessage) x).messsage);
                    }                
                    else { // There are no other kinds of messages, so
                           // quit silently now.
                        throw new BadMessageException("Undefined message");                        
                    }
                    if ( reply != null ) con.writeMessage(reply);                 
                }
                catch(MessageHandlerException mx) {
                    con.writeMessage(new Message.Error(mx));
                }
            }                                    
        }
        catch(BadMessageException bx) {
            // The can be thrown by con.readMessage() if it gets something
            // other than a serializeable object.  In this case we silently
            // close the connection.
        }
        catch(Exception xxx) {
            System.err.println(xxx);
        }
        finally {
            if ( con != null ) con.close();
        }
    }
    
        // -------------------------------------------
    
    Message addUser(Message.AddUser m) throws MessageHandlerException {
        
        try {
                // Encrypt user name.
            String eid = S3Key.createPeidKey(m.uid);

                // Create ref strings
            String dts = TimeUtil.toDateTimeString(System.currentTimeMillis());
            String callerReference = buildCallerReference(m.uid,dts);
            String callerReferenceSender = buildCallerReferenceSender(m.uid,dts);
            String callerReferenceSettlement = buildCallerReferenceSettlement(m.uid,dts);

                // Create the full post authorization url
            String url = postAuthorizationUrl + "/" + UserMonitor.POST_AUTH_URI_FLAG +
                         "/" + m.uid;
            
                // Create url
            PostPaidPipeline ppp = 
                new PostPaidPipeline(paramEc2.getAwsAccessKey(),paramEc2.getAwsSecretKey());
            ppp.setParameters(url,
                              callerReference,
                              callerReferenceSender,
                              callerReferenceSettlement,
                              Integer.toString(GLOBAL_AMOUNT_LIMIT_DEFAULT),
                              Integer.toString(CREDIT_LIMIT_DEFAULT),
                              PAYMENT_REASON,
                              "USD",
                              "CC,ABT,ACH",
                              "4481049601" 
                              // ^ As usual the aws doc is wrong. Omitting set a one year 
                              // expiration date. The date needs to be in 
                              // 'Epoch Time'. This value=Jan 1 2112
            );
                                                 
                // Add user to db, status pending
            userDb.addUser(m.uid, eid, 
                           callerReference, 
                           callerReferenceSender, 
                           callerReferenceSettlement);            
            
                // Record the user in UserStatus with status pending
            userStatus.putStatus(eid, UserStatus.Status.pending);
            
                // Create the reply, which contains the url
            return(new Message.NewUserUrl(m.uid,eid,ppp.getUrl()));
        }
        catch(Exception xx) { throw new MessageHandlerException(xx); }
    }
    
    String buildCallerReference(String uid,String dts) {
        return("callerRef_" + uid + "_" + dts);
    }
    
    String buildCallerReferenceSender(String uid,String dts) {
        return("callerRefSender_" + uid + "_" + dts);
    }
    
    String buildCallerReferenceSettlement(String uid,String dts) {
        return("callerRefSettlement_" + uid + "_" + dts);
    }
    
    private static String PAYMENT_REASON =
         "You will be charged " + ConstantsUM.PRICE_PIPH +
         " US per instance per hour for using " +
         "Northbranchlogic-ppe AMIs. Please note " +
         "that this is in addition to Amazon's charges for instances and " +
         "data tranfers.  You can cancel this authorization at any time by " +
         "using 'Account -> Cancel authorization for instance billing' in the " +
         "ec2 network manager application or by sending email to " +
         "billing@norbl.com. These AMI charges will be billed separately from " +
         "Amazon's charges.  You will be billed monthly if and when " +
         "the amount due is > $10.00 US.  If your total never exceeds $10.00 " +
         "you will never be billed and will not owe anything.";
    
    Message recordBillableUsage(Message.BillableUsage m) 
        throws SDBAccessException {        
        if ( UserMonitor.verbose ) System.out.println("   inc'ing usage for " + m.uid);
        userDb.incrementNInstanceHours(m.uid);   
        if ( UserMonitor.verbose ) System.out.println("   inc'ed usage for " + m.uid);
        return(new Message.Ok());
    }
}
