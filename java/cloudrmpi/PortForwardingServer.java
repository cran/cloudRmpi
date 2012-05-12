/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of Remove R Evaluator (rreval).

    rreval is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    rreval is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with rreval.  If not, see <http://www.gnu.org/licenses/>.
 */
package cloudrmpi;

import com.norbl.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import rreval.*;
import com.norbl.util.ssh.*;
import ch.ethz.ssh2.*;

/** This class manages ssh port forwarding.  It is implemented as a server 
 *  that responds to text commands from R, send via socket.
 *  It is used to start and stop ssh port forwarding.<p>
 * 
 *  This server accepts an unlimited number of connections, which remain
 *  open until the client explicitly closes them, or the app is shutdown.
 *
 * @author Barnet Wagman
 */
public class PortForwardingServer {

    public static final int CLIENT_PORT_DEFAULT = 4470;
    
    public static long SSH_WAIT_TIME = 5000L;
    
    public static final String ARGV_PORT = "port";
    public static final String ARGV_PEMFILE = "pemfile";
    
    public static final String CMD_LABEL_PORT_LOCAL = "portlocal";
    public static final String CMD_LABEL_PORT_REMOTE = "portremote";
    public static final String CMD_LABEL_PEMFILE = "pemfile";
    public static final String CMD_LABEL_HOST = "host";
    public static final String CMD_LABEL_USER_NAME = "username";
    
    enum Cmd {
        startPortForwarding, 
        terminatePortForwarding,
        terminateAllPortForwarding,
        closeCmdConnection,
        shutdown, 
        test
    }
    
    int port;
    boolean keepRunning;
    ServerSocket serverSocket;
    List<MessageHandler> messageHandlers;    
    HashMap<String,Pf> pfHt;
    
    public PortForwardingServer(int port) {
        this.port = port;        
        messageHandlers = new ArrayList<MessageHandler>();
        pfHt = new HashMap<String, Pf>();
    }
    
    /**
     * 
     * @param argv 
     */
    public static void main(String[] argv) {
        
        int port = ArgvUtil.getIntVal(argv, "port", CLIENT_PORT_DEFAULT);      
       
        PortForwardingServer pfs = new PortForwardingServer(port);
    
        Verbose.verbose = 
            Boolean.parseBoolean(ArgvUtil.getVal(argv, "verbose", "true"));
        
        pfs.launch();
    }
    
    public void launch() {
        try {
                    
            keepRunning = true;
            serverSocket = new ServerSocket(port);
            
            while (keepRunning) {
                try {                    
                    Socket s = serverSocket.accept();
                    MessageHandler h = new MessageHandler(s);
                    messageHandlers.add(h);
                    (new Thread(h)).start();
                    Verbose.show("... R client has connected on port "+ port);
                }
                catch(Exception xxx) {
                    Verbose.show("PortForwardingServer.launch(): ",xxx);                 
                }
            }
        }
        catch(Exception xxx) { Verbose.show("PortForwardingServer.launch(): ",xxx); }
    }
    
        // ---------------------------------------------------
    
    /** This class does all the work. The commands are {@link AppCmd}s
     *  packed in {@link Message}s of message type {@link MessageType#cj}.
     *  A response is sent is response to each cmd, {@link MessageType#rj}.
     * 
     */
    class MessageHandler implements Runnable {
        
        Socket socket;
        ConnectionR conR;
        boolean keepHandlerRunning;
        

        public MessageHandler(Socket socket ) throws IOException {
            try {
                this.socket = socket;
                conR = new ConnectionR(socket);
            }
            catch(IOException iox) {
                closeCmdConnection();
                throw iox;
            }
        }
        
        public void run() {
            keepHandlerRunning = true;
            
            while (keepHandlerRunning) {
                
                Message m = null;
                
                try {
                    m = conR.readMessage();
                    
                    if (  MessageType.cj.equals(m.type) ) {
                        AppCmd ac = new AppCmd(m.obj);
                        respondToCommand(ac);
                    }
                    else {                          
                        conR.writeMessage(
                            new Message(MessageType.er,
                                "Incorrect message type= " + 
                                 StringUtil.toStringNull(m.type)
                            ));
                    }                   
                }
                catch(Exception xx) {
                    if ( xx instanceof RReaderEOFException ) {
                        Verbose.show("PortForwardingServer: Error reading client message",xx);
                        closeCmdConnection();                        
                        return;
                    } 
                    // Else ignore, ack has been sent.
                }                
            }
       }
        
       void respondToCommand(AppCmd ac) {               
           
            try {                                
                Cmd c = Cmd.valueOf(ac.getCmdName());
                               
                switch(c) {
                    case startPortForwarding: 
                        startPortForwarding(ac);
                        break;
                    case terminatePortForwarding:
                        terminatePortForwarding(ac);
                        break;
                    case terminateAllPortForwarding:
                        terminateAllPortForwarding();
                        break;
                    case closeCmdConnection:
                        closeCmdConnection();    
                        break;
                    case shutdown:
                        shutdownApp();
                        break;
                    case test:
                        test(ac);
                    default:
                        
                }                
            }
            catch(IllegalArgumentException iax) {
                sendMessageToR("Undefined cmd=" + ac.getCmdName());
            }
            catch(Exception xxx) { sendErrMToR(xxx); }
       } 
       
       void shutdownApp() {
           sendMessageToR("Shutting down the ssh portforwarding app; " +
                          "all portforwarding has been shutdown.");
           System.exit(0);
       }
       
       void test(AppCmd ac) {
           String mess = ac.getVal("message");
           System.err.println("TEST MESSAGE: " + mess);
           sendMessageToR("REPLY: " + mess);
       }
       
       void startPortForwarding(AppCmd ac) {
            try {
                String hostName = ac.getVal(CMD_LABEL_HOST);
                String userName = ac.getVal(CMD_LABEL_USER_NAME);
                File pemFile = new File(ac.getVal(CMD_LABEL_PEMFILE));
                int portLocal = Integer.parseInt(ac.getVal(CMD_LABEL_PORT_LOCAL));
                int portRemote = Integer.parseInt(ac.getVal(CMD_LABEL_PORT_REMOTE));
                
                if ( !pemFile.exists() ) {
                    sendMessageToR("PEM file " + pemFile.getPath() + 
                                   " does not exist.");
                    return;
                }
                
                if ( (hostName == null) || (userName == null) ||
                     (portLocal <= 0) || (portRemote <= 0) ) {
                    sendMessageToR("One or more bad params: host=" + hostName + 
                                   " user=" + userName + 
                                   " portremote=" + portRemote +
                                   " portlocal=" + portLocal 
                                  );
                                    
                }
                
                
                if ( pfHt.containsKey(buildPfKey(hostName, userName)) ) {
                    sendMessageToR(buildPfKey(hostName, userName));
                }
                else {
                        // Make sure that the local port is not in use
                    if (portInUse(portLocal) ) {
                        sendMessageToR("Local port " + Integer.toString(portLocal) +
                                       " is already being used.");
                    }
                    
                    Pf pf = new Pf(hostName, userName, portRemote, pemFile, portLocal);
                    pfHt.put(pf.getKey(),pf);
                    sendMessageToR(pf.getKey());
                }                
            }
            catch(Exception xxx) { sendErrMToR(xxx); }                       
       }
       
       private boolean portInUse(int port) {
            // Block attempt to use default ports
           if ( (port == CLIENT_PORT_DEFAULT) ||
                (port == 4460) || (port == 4464) || (port == 4461) ) {
               return(true);
           }
           
           for ( Pf pf : pfHt.values() ) {
               if ( pf.portLocal == port ) return(true);
           }
           return(false);
       }
     
       
       void terminatePortForwarding(AppCmd ac) {
           try {
                String hostName = ac.getVal(CMD_LABEL_HOST);
                String userName = ac.getVal(CMD_LABEL_USER_NAME);
                
                if ( (hostName == null) || (userName == null) ) {
                    sendMessageToR("One or more bad params: host=" + hostName + 
                                   " user=" + userName);
                    return;
                }
                
                String key = buildPfKey(hostName, userName);
                Pf pf = pfHt.get(key);
                if ( pf != null ) {
                    pf.close();
                    pfHt.remove(key);
                    sendMessageToR("Closed connection to " + key);                    
                }
                else {
                    sendMessageToR("No connection to " + key);
                }                
           }           
           catch(Exception xxx) { sendErrMToR(xxx); }  
       }
       
       
       void terminateAllPortForwarding() {
           
           try {
                for ( Pf pf : pfHt.values() ) {
                    pf.close();
                }

                pfHt.clear();
                sendMessageToR("All port forwarding has been terminated.");
           }
           catch(Exception xxx) { sendErrMToR(xxx); }  
              
       }
        
       void closeCmdConnection() {      
            try {           
                if ( conR != null ) conR.close();
                if ( socket != null ) socket.close();           
            }
            catch(IOException iox) {
                Verbose.show(iox);
            }
            finally {
                 keepHandlerRunning = false;
                 for ( MessageHandler h : messageHandlers ) {
                     messageHandlers.remove(h);
                     return;
                 }
            }
       }
        
       protected void sendErrMToR(String m) {
            try {
                conR.writeMessage(new Message(MessageType.er,m));
                Verbose.show("ClientCmdHandler sending error message to " +
                                "client failed: " + m);
            }
            catch(Exception z) { 
                    System.err.println(StringUtil.toString(z));
            }
        }  

        protected void sendErrMToR(Exception x) {
            sendErrMToR(StringUtil.getExceptionMessage(x));
        }

        protected void sendErrMToR(String prefix, Exception x) {
            sendErrMToR(prefix + ": " + 
                        StringUtil.getExceptionMessage(x) + "\n" +
                        StringUtil.exceptionStackToString(x));
        } 
    
        protected void sendMessageToR(String m) {
            try {
                if ( (m == null) || (m.length() < 1) ) m = " ";
                conR.writeMessage(new Message(MessageType.rj,m));          
            }
            catch(Exception z) { 
                    System.err.println(StringUtil.toString(z));
            }
        }    
    }
    
    class Pf {
        
        String hostName;
        String userName;
        int portLocal, portRemote;
        File pemFile;
        
        Connection con;
        LocalPortForwarder portForwarder;
        
        Pf(String hostName, String userName, int portRemote, File pemFile,
           int portLocal) 
            throws IOException, ConnectFailureException {
            
            this.hostName = hostName;
            this.userName = userName;
            this.portRemote = portRemote;
            this.pemFile = pemFile;
            this.portLocal = portLocal;
            
            con = Ssh.connect(hostName, userName, pemFile, SSH_WAIT_TIME);
            portForwarder = con.createLocalPortForwarder(portLocal, 
                                                       "localhost",
                                                       portRemote);
            Verbose.show("PortForwarind server: started port forwarding to " +
                     userName + "@" + hostName + " ports " +
                     portLocal + " -> " + portRemote);
        }
        
        String getKey() {
            return(buildPfKey(hostName, userName));            
        }
        
        void close() throws IOException {
            if ( portForwarder != null ) portForwarder.close();
            if (con != null ) con.close();
        }
    }
    
    String buildPfKey(String hostName,String userName) {
        return(userName + "@" + hostName);
    }
}
