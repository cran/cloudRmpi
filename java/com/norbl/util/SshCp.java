/*
    Copyright 2011 Northbranchlogic, Inc.

    This file is part of Parallel Processing with EC2 (ppe).

    ppe is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ppe is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.norbl.util;

import com.norbl.cbp.ppe.*;
import ch.ethz.ssh2.*;
import java.io.*;
import java.util.*;

/** Copies files between to remote systems using Ganymed's scp.
 *  This actually requires getting the file to the local system
 *  and then putting it to the recipient.<p>
 *
 *
 * @author Barnet Wagman
 */
public class SshCp {
    
    File tmpDir;
//    Log log;    

    public SshCp() {
        tmpDir =  FileUtil.getTmpDir(ConstantsPPE.TMP_DIR);
//        log = LogFactory.getLog(NearNilLog.class);
    }

         /** Copies the specified files to set of hosts.  The file
          *  is copied to the same place in the directory tree on all
          *  the recipients.
          *
          * @param recipientHosts
          * @param srcHost
          * @param srcDir
          * @param filename
          * @param htc
          * @throws IOException
          */
    public void cp(HashMap<String,Connection> htc,
                   List<String> recipientHosts,
                   String srcHost, 
                   String srcDir, String filename)
        throws IOException {

        if ( recipientHosts.size() < 1 ) return;

        String filePath = 
            convertToUnixPath((new File(srcDir,filename) ).getPath());
        // This is a path on the remote host, so it must be a Unix path.
        // BUT getPath() uses the local dir delim, so on a Windows
        // system it uses '\'.  The conver...() method fixes this.
       
        File localCopy = new File(tmpDir,filename);

        // Download the file
        SCPClient srcScp = new SCPClient(htc.get(srcHost));
        srcScp.get(filePath, tmpDir.getPath());
        
        if ( !localCopy.exists() ) {
            throw new IOException("SshCp.cp(): local copy " + localCopy.getPath() +
                    " does not exist; download failed.");
        }

            // Upload copies
        for ( String rHost : recipientHosts ) {
            cp(htc.get(rHost),srcDir,localCopy);
        }       
    }

    public void cp(Connection connection,String remoteTargetDirectory,
                   File localFile) throws IOException {
        SCPClient scp = new SCPClient(connection);
        scp.put(localFile.getPath(),remoteTargetDirectory);
    }
    
    public void download(Connection con, String localDir, String remoteFile) 
        throws IOException {
        
        SCPClient srcScp = new SCPClient(con);
        srcScp.get(remoteFile,localDir);                
    }
    
    static String convertToUnixPath(String p) {
        if ( !p.contains("\\") ) return(p);
        else return(p.replace("\\","/"));
    }
}
