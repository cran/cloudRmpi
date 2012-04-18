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
    public String exec(String cmd) throws IOException {
       
        Session s = null;
        try {
            s = connection.openSession();          
            BufR std = new BufR(s.getStdout());
            BufR stderr = new BufR(s.getStderr());          
            s.execCommand(cmd);            
         
            String str = std.readCompletely();
            String err = stderr.readCompletely();

            if ( err != null ) str += "\n" + err;          
            return(str);
        }
        finally {
            if ( s != null ) s.close();
        }
    }

    public void execNoRead(String cmd) throws IOException {

        Session s = null;
        try {
            s = connection.openSession();
            BufR std = new BufR(s.getStdout());
            BufR stderr = new BufR(s.getStderr());
            s.execCommand(cmd);
        }
        finally {
//            if ( s != null ) s.close();
        }
    }

    public void closeConnection() {
        if ( connection != null ) connection.close();
    }

    public static String exec(Connection connection,String cmd) 
        throws IOException {

        SshExec sx = new SshExec(connection);
        return(sx.exec(cmd));
    }

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

    public boolean fileExists(String path) throws IOException {
        String r = exec("[ -f " + path + "  ] && echo TRUE || echo FALSE");
        return( r.trim().equals("TRUE") );
    }
}
