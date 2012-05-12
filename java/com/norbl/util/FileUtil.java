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

import com.norbl.util.gui.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class FileUtil {
    
    public static List<String[]> readConfigTypeFile(File f,
                                                    String fieldDelimiterExpr,
                                                    String commentMarker,
                                                    int nFields)
        throws FileNotFoundException, IOException {
        FileReader r = null;
        BufferedReader b = null;
        try {
            r = new FileReader(f);
            b = new BufferedReader(r);

            List<String[]> lines = new ArrayList<String[]>();
            String line;

            NEXT: while ( (line = b.readLine()) != null ) {
                String ln = line.trim();
                if ( ln.startsWith(commentMarker) ) continue NEXT;     
                if ( ln.length() < ((2*nFields) - 1) ) continue NEXT;
                if ( nFields > 0 ) lines.add( ln.split(fieldDelimiterExpr,nFields)  );                   
                else lines.add( ln.split(fieldDelimiterExpr)  );
            }
            return(lines);
        }
        finally {
            try {
                if ( b != null ) b.close();
                if ( r != null ) r.close();
            }
            catch(IOException iox) { throw new RuntimeException(iox); }
        }
    }
    
    public static void writeConfigFile(File f, List<String[]> lines, String delim) 
        throws IOException {
        
        FileWriter w = null;
        BufferedWriter b = null;
        try {
            w = new FileWriter(f);
            b = new BufferedWriter(w);
            for ( String[] line : lines ) {
                for ( int i = 0; i < line.length-1; i++ ) {
                    b.write(line[i] + delim);
                }
                b.write(line[line.length-1] + "\n");
            }            
        }           
        finally {
            if ( b != null ) {
                b.flush();
                b.close();
            }
            if ( w != null ) { w.close(); }
        }
    }
    
    public static List<String[]> readConfigTypeFile(File f,
                                                    String fieldDelimiterExpr,
                                                    String commentMarker)
        throws FileNotFoundException, IOException {
        return( readConfigTypeFile(f, fieldDelimiterExpr, commentMarker, -1) );
    }

    public static File getTmpDir(String tmpDirName) {
        File tmpDir = new File(SysProp.user_home.getVal(),tmpDirName);
        if ( !tmpDir.exists() ) {
            if ( !tmpDir.mkdirs() ) {
                throw new RuntimeException(tmpDir.getPath() +
                        " does not exist and " +
                        " an attempt to create it failed.");
            }
        }
        if ( !tmpDir.canWrite() )
            throw new RuntimeException(tmpDir.getPath() +
                                       " is not write accessible.");
        else return(tmpDir);
    }
    
    /** Creates a <tt>File</tt> based on <tt>f</tt> that does 
     *  not refer to an existing file.
     * 
     * @param f
     * @return 
     */
    public static File getBackupFilename(File f) {
        File bu = null;
        int cnt = 0;
        do {
            String parent = f.getParent();
            String fname = f.getName();
            int idx = fname.lastIndexOf(".");
            String name = null;
            String ext = null;
            if ( idx > 0 ) {
                    // ^ If idx == 0, we have a hidden file with no ext.
                    //   In that case we want to append to the entire name.
                name = fname.substring(0, idx);
                ext = fname.substring(idx);                
            }
            else {
                name = fname;
                ext = "";
            }
            bu = new File(parent, 
                          name + "_" + TimeUtil.toYMD(System.currentTimeMillis()) +
                          "_" + Integer.toString(cnt) + ext);
            ++cnt;
        }
        while (bu.exists());
        return(bu);
    }
    
    public static int write(File file, String s) {
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(s);
            fw.flush();
            return(s.length());
        }
        catch(Exception xxx) {
            GuiUtil.exceptionMessageOnly(xxx);
            return(-1);
        }
        finally {
            if ( fw != null ) {
                try {
                    fw.flush();
                    fw.close();
                }
                catch(Exception xxx) {}
            }
        }        
    }
}
