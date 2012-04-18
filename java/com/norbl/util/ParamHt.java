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
import java.util.*;
import java.io.*;
import com.norbl.util.gui.*;

/** A hash table of string param values keyed by parm names. Keys
 *  are stored as lowercase. This
 *  class contains the methods for reading param files and
 *  parsing argv. This class does not check or interpret params in any
 *  way - that is the responsibility of concrete subclasses of
 *  {@link AbstractParams} that use this class as a source of param values.
 * 
 *  Concrete subclasses typically have a hardcoded <tt>configFilenameDefault</tt>.
 *
 * @author Barnet Wagman
 */
abstract public class ParamHt extends HashMap<String,String> {

   public enum ParamName { configFile };

   public File configFile;
   public String configFilenameDefault;

   public ParamHt(String[] argv, String configFilenameDefault)
        throws InaccessibleFileException, IOException {
        super();
              
        this.configFilenameDefault = configFilenameDefault;
       
            // Set the config file to it's default value.
        String configFilePath = getDefaultConfigFilePath(configFilenameDefault);
        setConfigFile(configFilePath);

           // If the config file is specified in argv, get it.
        String configFilename = getVal(argv,ParamName.configFile);
        if ( configFilename != null ) {
            setConfigFile(configFilename);            
        }
        
            // If a config file exists, load its contents.
        if ( (configFile != null) && configFile.exists() ) {
            recordParams( getParamsFromConfigFile() );
        }                

            // If argv is not empty, load its contents.
        if ( (argv != null) && (argv.length > 0) ) {
            recordParams(argv);
        }
        
            // If there is config file, store it the ht
//        if ( configFile != null )
//            put(configFile.toString(),configFile.getPath());
   }
      
   /** Loads params from the param file.
    * 
    * @return 
    */
   public boolean reloadParams() {
        try {
            if ( (configFile != null) && configFile.exists() ) {
                recordParams( getParamsFromConfigFile() );
                return(true);
            }
            else return(false);
        }
        catch(Exception x) {
            GuiUtil.exceptionMessage(x);
            return(false);
        }
   }

       // --------------------------------------------
   
   public static String getDefaultConfigFilePath(String configFilename) {
        return( SysProp.user_home.getVal() + "/" + configFilename );
   }
   
   private String getVal(String[] argv, ParamName par) {

        String ps = par.toString().toLowerCase() + "=";
        for ( String arg : argv ) {
            if ( arg.toLowerCase().startsWith(ps) ) {
                String[] a = arg.split("=");
                if ( (a != null) || (a.length == 2) )
                    return(a[1]);
            }
        }
        return(null);
    }

        /** Note that if the specified file does not exist, this method
         *  does nothing and does not throw an exception.
         *
         * @param path
         * @throws InaccessibleFileException if the specified file exists
         *  but is not readable or is a directory.
         */
    private void setConfigFile(String path) throws InaccessibleFileException {

        File f = new File(path);
        if ( f.exists() && !f.isDirectory() ) {
            if ( f.canRead() ) configFile = f;
            else throw new InaccessibleFileException("The config file " +
                    f.getPath() + " exists but is not readable by this application.");
        }
    }

        /** Lines starting with '#' are ignored.  Trailing comments are not
         *  supported because '#' may be a legal character in the secret key
         *  (sigh).
         * @return
         * @throws IOException
         */
    private String[] getParamsFromConfigFile() throws IOException {

        BufferedReader buffy = new BufferedReader(new FileReader(configFile));

        List<String> lst = new ArrayList<String>();
        String ln = null;
        NEXT: while ( (ln = buffy.readLine()) != null ) {
            String pair = ln.trim();
            if ( pair.startsWith("#") ) continue NEXT; // Comment
            else if (pair.contains("=")) lst.add(pair);
        }

        return( lst.toArray(new String [lst.size()]) );
    }

    void recordParams( String[] argv ) {
        if ( argv == null ) return;
        for ( String arg : argv ) {
            String[] a = arg.split("=");
            if ( (a != null) && (a.length == 2) ) {
                put(encase(a[0]),a[1]); // setValue(a[0],a[1]);
            }
        }
    }
    
    public void writeToConfigFile() { writeToConfigFile(true); }
    
    /** Writes the current params to the config file.
     *  If a configFile exists, it is renamed per
     *  {@link FileUtil#getBackupFilename(java.io.File) } and
     *  the updated params are written to the specified file name.
     * 
     * @param configFile 
     */
    public void writeToConfigFile(boolean showFileWrit) {
     
        BufferedWriter b = null;
        try {
            File backupFile = null; 
            File cf = null;
            
            if ( configFile == null ) {
                configFile = new File(ParamHt.getDefaultConfigFilePath(
                                      this.configFilenameDefault));
            }                                      
            
            cf = new File(configFile.getPath());
                       
            if ( cf.exists() ) {
                backupFile = FileUtil.getBackupFilename(cf);
                if ( !cf.renameTo(backupFile) ) {
                    GuiUtil.warning(new String[] {
                        "Unable to rename " + cf.getPath() + "; changes were not saved." },
                         "File rename error");
                    return;
                }     
            }
        
            b = new BufferedWriter(new FileWriter(configFile));
            for ( Iterator<String> it = this.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                String val = this.get(key); //getValue(key);               
                if ( val != null) {
                    b.write(key + "=" + val + "\n");                  
                }
            }            
            b.flush();
            if ( showFileWrit ) {
                GuiUtil.info(new String[] {
                    "Wrote params to config file " + configFile.getPath(),
                    (backupFile != null)?("The old version was saved as " + backupFile.getPath())
                                    :"" },
                    "Updated config file");  
            }
        }
        catch(IOException iox) { GuiUtil.exceptionMessage(iox); }
        finally {
            if ( b != null ) {
                try { 
                    b.flush();
                    b.close();
                }
                catch(IOException ox) { GuiUtil.exceptionMessage(ox); }               
            }
       }                
    }
    
    /** Writes params to the config file then reloads the params from that file.
     * 
     */
    public void saveAndReload(boolean showFileWrit) {
        writeToConfigFile(showFileWrit);
        reloadParams();
    }
    
    public void saveAndReload() {
        saveAndReload(true);
    }
    
    public void show() {
        for ( Iterator<String> it = this.keySet().iterator(); it.hasNext(); ) {
            String k = it.next();
            System.out.println(k + "=" + this.get(k));
        }
    }
    
    
    // The following convert caseless ec2 param names to cased versions.
    // This allows the use of old config files.
    
    private static HashMap<String,String> epHt;
    
    static {
        epHt = new HashMap<String,String>();
        for ( ParamsEc2.ParamName pn : ParamsEc2.ParamName.values() ) {
            epHt.put(pn.toString().toLowerCase(),pn.toString());
        }
    }   
    
    private String encase(String key) {
        if ( epHt.containsKey(key.toLowerCase()) )
            return( epHt.get(key.toLowerCase()) );
        else return(key);
    }
}
