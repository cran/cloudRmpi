/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of utilssh.

    utilssh is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    utilssh is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.norbl.util.ssh;

import ch.ethz.ssh2.*;
import com.norbl.cbp.ppe.*;
import java.io.*;

/** Methods for remotely executing commands via ssh.  It includes
 *  a general exec method and a few specialized ones (such as
 *  creating an rsa keypair.
 *
 * @author Barnet Wagman
 */
public class SshExec {
   
    protected Connection connection;

    public SshExec(Connection connection)
        throws IOException {      
        this.connection = connection;
    }

    /** Note that 
     * 
     * @param cmd connection.execCommand() does not return until the
     *        exec is complete.  If it calls an app that runs forever.
     *        it never returns.
     * @return
     * @throws IOException 
     */
//    public String execRead(String cmd) throws IOException {
//       
//        Session s = null;
//        try {
//            s = connection.openSession();          
//            BufR std = new BufR(s.getStdout());
//            BufR stderr = new BufR(s.getStderr());          
//            s.execCommand(cmd);            
//         
//            String str = std.readCompletely();
//            String err = stderr.readCompletely();
//
//            if ( err != null ) str += "\n" + err;          
//            return(str);
//        }
//        finally {
//            if ( s != null ) s.close();
//        }
//    }
    
    public String execRead(String cmd, long maxWait) throws IOException {
     
        ExecReader er = new ExecReader(cmd,maxWait);
        er.doEr();
        try {
            if ( er.stdout.s != null ) return(er.stdout.s);
            else return(er.stderr.s);
        }
        catch(Exception xxx) {
            PPEApp.verbose(xxx);
            return(null);
        }
    }

    /**
     * 
     * @param cmd
     * @throws IOException 
     * @deprecated
     */
    public void execNoRead(String cmd) throws IOException {

        Session s = null;
        try {
            s = connection.openSession();
//            BufR std = new BufR(s.getStdout());
//            BufR stderr = new BufR(s.getStderr());
            s.execCommand(cmd);
        }
        finally {
//            if ( s != null ) s.close();
        }
    }
    
    /** Blocks until the command has executed or an exception is thrown.
     *  This method does not capture stdout or stderr output.
     * 
     * @param cmd
     * @param maxWait
     * @throws IOException 
     */
    public void exec(String cmd, long maxWait) throws IOException {
        
        ExecThread et = new ExecThread(connection.openSession(), cmd);
        (new Thread(et)).start();
        
        long tN = System.currentTimeMillis() + maxWait;
        while ( !et.done && (System.currentTimeMillis() < tN) ) {
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }
        
        if ( !et.done ) PPEApp.verbose("Timed out execing " + cmd);
    }
    
    class ExecThread implements Runnable {
        Session s;
        String cmd;
        boolean done;
        ExecThread(Session s, String cmd) {
            this.s = s;
            this.cmd = cmd;
        }
        
        public void run() {
            try {
                done = false;
                s.execCommand(cmd);
            }
            catch(Exception xxx ) {
                System.out.println("ExecThread cmd=" + cmd + " -> " + xxx);
            }
            finally {
                done = true;
                if ( s != null ) s.close();
            }            
        }
    }

    public void closeConnection() {
        if ( connection != null ) connection.close();
    }

//    public static String exec(Connection connection,String cmd) 
//        throws IOException {
//
//        SshExec sx = new SshExec(connection);
//        return(sx.exec(cmd));
//    }

    class BufR {
        InputStream ins;
        InputStreamReader r;
        BufferedReader b;

        BufR(InputStream ins) {
            this.ins = ins;
            this.r = new InputStreamReader(ins);
            this.b = new BufferedReader(r);
        }

        String readCompletely() throws IOException {

            StringBuilder s = new StringBuilder();
            String l;

            while ( (l = b.readLine()) != null ) {
                s.append(l + "\n");
            }
            return(s.toString());
        }

        void close() throws IOException {
            b.close();
            r.close();
            ins.close();
        }
    }

    public boolean fileExists(String path,long maxWait) throws IOException {
        String r = execRead("[ -f " + path + "  ] && echo TRUE || echo FALSE",
                            maxWait);
        return( r.trim().equals("TRUE") );
    }
    
    class ExecReader {
        
        String cmd;
        Session s;
        long maxWait;
        ReadThread stdout, stderr;
        
        ExecReader(String cmd, long maxWait) throws IOException {
            this.cmd = cmd;
            this.maxWait = maxWait;
            s = connection.openSession();
        }
        
        public void doEr() throws IOException {
            try {
                stdout = new ReadThread(s.getStdout());
                stderr = new ReadThread(s.getStderr());

                (new Thread(stdout)).start();
                (new Thread(stderr)).start();

                s.execCommand(cmd);

                long tN = System.currentTimeMillis() + maxWait;
                while ( !stdout.done && !stderr.done &&
                        (System.currentTimeMillis() < tN) ) {
                    try { Thread.sleep(1000L); }
                    catch(InterruptedException irx) {}
                }
                if ( !stdout.done && !stderr.done )
                    PPEApp.verbose("Timed out exec-ing " + cmd);
            }
            finally {
                if ( s != null ) s.close();
            }
        }
        
        public String getRead() {
            if ( stdout.s != null ) return(stdout.s);
            else return(stderr.s);
        }
        
    }
    
    class ReadThread implements Runnable {
        
        InputStream ins;
        String s;
        Exception iox;
        boolean done;
        
        ReadThread(InputStream ins) {
            this.ins = ins;
            done = false;
        }
        
        public void run() {
            BufR br = null;
            try {                
                br = new BufR(ins);
                s = br.readCompletely();
                done = true;
            }
            catch(IOException iox) {
                this.iox =  iox;
                done = true;
            }
            finally {
                try {
                    if ( br != null ) br.close();
                }
                catch(Exception xxx) { PPEApp.verbose(xxx);}
            }
        }
    }
}
