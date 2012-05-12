/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.util.ssh;

import ch.ethz.ssh2.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class SshMinimalShell {
       
    protected Connection connection;
    protected Session session;
    
    StdReader stdout, stderr;
    BufW stdin;
    SshMinimalShellFrame frame;
    StdinHandler stdinHandler;        
    
    public SshMinimalShell(Connection connection) {                     
        this.connection = connection;        
    }
    
    public void connect() throws Exception {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame = new SshMinimalShellFrame(connection.getHostname(),
                                                 SshMinimalShell.this);              
                frame.appendOutput("Connecting to " + 
                                   connection.getHostname() + "\n");
            }
        });
        
        session = connection.openSession();
        session.requestPTY("dumb");
        session.startShell();
                
        stdin = new BufW(session.getStdin());
        stdout = new StdReader(session.getStdout());
        stderr = new StdReader(session.getStderr());
        
        stdinHandler = new StdinHandler();
        while ( frame == null ) {
            try { Thread.sleep(100L); } catch(InterruptedException ix) {}
        }
        frame.getStdinDoc().addDocumentListener(stdinHandler);       
        frame.setInputKeyListener(stdinHandler);
        (new Thread(stdout)).start();
    }
    
    void sendCmd(String c) throws Exception {       
        stdin.writeLine(c);      
    }
    
    public void shutdown() {
        if ( stdout != null ) stdout.close();
        if ( stdin != null ) stdin.close();
        if ( stderr != null ) stderr.close();
        if ( session != null ) session.close();
        if ( connection != null ) connection.close();
    }
    
        // ---------------------------------------------------------
    
    class StdReader implements Runnable {
        InputStream ins;
        InputStreamReader r;                     
        
        boolean keepRunning;

        StdReader(InputStream ins) {
            this.ins = ins;
            this.r = new InputStreamReader(ins);            
            keepRunning = true;
        }

        public void run() { 
            try {
                while ( keepRunning ) {                   
                    frame.appendOutput(Character.toString((char) r.read()));
                }
            }
            catch(Exception xxx) {
                GuiUtil.exceptionMessage(xxx);
                keepRunning = false;
                return;
            }
        }
                
        void close() {
            try {
                keepRunning = false;          
                if ( r != null ) r.close();
                if ( ins != null ) ins.close();
            }
            catch(Exception xxx) {}
        }
    }
    
    class BufW {
        OutputStreamWriter ow;
        BufferedOutputStream b;
        
        BufW(OutputStream ous) {
            b = new BufferedOutputStream(ous);
            ow = new OutputStreamWriter(b);            
        }
        
        void writeLine(String s) throws Exception {
            ow.write(s, 0, s.length());
            ow.flush();
        }
        
        void close() {
            try {
                if ( b != null ) b.close();
                if ( ow != null) ow.close();           
            }
            catch(Exception xxx) {}
        }
    }
    
    
    static int MAX_HIST = 1000;
    
    class StdinHandler implements DocumentListener, KeyListener {  
                        
        List<String> cmdHistory;
        int idxHistory;

        public StdinHandler() {
            cmdHistory = new ArrayList<String>();
            idxHistory = cmdHistory.size();
        }                
                           
        public void insertUpdate(DocumentEvent de) {
            try {
                final Document doc = de.getDocument();
                String s = doc.getText(doc.getLength()-1,1);
                if ( s.contains("\n") ) {
                    String line = getLastLine(doc);
                    if ( (line != null) && (line.length() > 0 ) ) {
                        sendCmd(line);
                        cmdHistory.add(line);                        
                        if ( cmdHistory.size() > MAX_HIST ) {
                            for (int i = 0; i < 100; i++ ) cmdHistory.remove(0);
                        }
                        idxHistory = cmdHistory.size();
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    doc.remove(0,doc.getLength());                                    
                                }
                                catch(Exception zzz ) { 
                                    GuiUtil.exceptionMessage(zzz); }
                            }
                        });
                    }
                }      
            }
            catch(Exception xxx) { GuiUtil.exceptionMessage(xxx); }
        }        
       
        public void removeUpdate(DocumentEvent de) {}        
        public void changedUpdate(DocumentEvent de) {} 
        
        String getLastLine(Document doc) throws Exception {            
            int n = doc.getLength();
            if ( n < 2 ) return(null);
            String s = doc.getText(0,n);
            int i0 = getStartOfLastLine(s,n);
            return( s.substring(i0) );
        }
        
        int getStartOfLastLine(String s, int lineLen) {            
            for ( int i = lineLen - 2; i >= 0; i-- ) {
                if ( s.charAt(i) == '\n' ) return(i+1);
            }
            return(0);
        }
    
        public void keyReleased(KeyEvent ke) {
            
            int keyCode = ke.getKeyCode();
            switch(keyCode) {
                case KeyEvent.VK_UP: 
                    showHistUp();
                    break;
                case KeyEvent.VK_DOWN:    
                    showHistDown();
                    break;
                default:
            }
        }
        
        void showHistUp() {
            
            if ( (idxHistory - 1) < 0 ) return;
            --idxHistory;
            if ( idxHistory >= cmdHistory.size() ) return;
            String hc = cmdHistory.get(idxHistory);
            showAsInput(hc);
        }
        
        void showHistDown() {
            
            if ( (idxHistory + 1) >= cmdHistory.size() ) return;
            ++idxHistory;            
            String hc = cmdHistory.get(idxHistory);
            showAsInput(hc);
        }
        
        void showAsInput(final String hc) {
            
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Document doc = frame.getStdinDoc();
                        doc.remove(0,doc.getLength());
                        frame.appendToStdin(hc.replace('\n',' ').trim());
                    }
                    catch(Exception xxx) { GuiUtil.exceptionMessage(xxx); }
                }
            });
        }
        
        public void keyPressed(KeyEvent ke) {}
        public void keyTyped(KeyEvent ke) {}
    }
    
    public static String helpTT = 
        "<html><div width=400><br>" +            
        "This is an admittedly minimal ssh shell. Enter commands in the 'stdin' " +
        "area and terminate them with newline - the results will be displayed " +
        "in 'stdout' and 'stderr' " +
        "<br><br>" +
        "There is a simple history mechanism: in stdin you can use the up and " +
        "down arrow keys to retrieve commands.<br>" +
        "<br>" +
        "When you launch this shell, you are logged in as ec2-user, but you " +
        "can use su or sudo (neither require a password).<br>" +
        "<br>" +     
        "And of course for a <i>real</i> shell you can always connect to any of " +
        "your instances with ssh, i.e." +
        "<blockquote>" + 
            "ssh -i &lt;pem file&gt; ec2-user@&lt;Public DNS (hostName)&gt;" +
        "</blockquote>" +
        "where &lt;pem file&gt; is the local file that contains your keypair." +
        "<br><br>" +
        "</div></html>";
    
    
    // .............................
    
    public static void main(String[] argv) throws Exception  {
        
        SwingDefaults.setDefaults();
        GuiMetrics.init();
        
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        ParamHt pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pe2 = new ParamsEc2(pht);
        
        System.out.println("Connecting " +  pe2.rsaKeyPairFile);
        Connection con = Ssh.connect("ec2-107-21-74-147.compute-1.amazonaws.com",
                              ConstantsEc2.EC2_USERNAME,
                              pe2.rsaKeyPairFile, // spec/rsaKeyPairFile,                              
                              1000L * 60L * 10L);
        System.out.println("Connected");
        SshMinimalShell st = new SshMinimalShell(con);
        st.connect();
        System.out.println("Session connected");
//        st.cmd("pwd");
//        st.cmd("ls");
//        st.cmd("pwd");
//        st.cmd("sudo fdisk -l");
        
        for(;;) {
            try { Thread.sleep(1000L); }
            catch(InterruptedException ix) {}
        }
//        
//        System.out.println("- fin -");
    }
} 
