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

package ppe;

import java.util.*;
import java.io.*;
import nbl.utilj.*;

/** A hash table of string param values keyed by param names. Keys
 *  are stored as lowercase. This
 *  class contains the methods for reading param files and
 *  parsing argv. This class does not check or interpret params in any
 *  way - that is the responsibility of concrete subclasses of
 *  {@link AbstractParams} that use this class as a source of param values.
 *
 * @author Barnet Wagman
 */
public class ParamHt extends HashMap<String,String> {

   public enum ParamName { configFile };

   public File configFile;

   public ParamHt(String[] argv)
       throws InaccessibleFileException, IOException {
       super();

            // Set the config file to it's default value.
        String configFilePath = getDefaultConfigFilePath();
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
        if ( configFile != null )
            put(configFile.toString().toLowerCase(),configFile.getPath());               
   }
   
   public void updateParams() {
        try {
            if ( (configFile != null) && configFile.exists() ) {
                recordParams( getParamsFromConfigFile() );
            }
            else GuiUtil.warning(new String[] {
                        "There is no param file, so params cannot be update.",
                        "Edit the ec2 params (Edit -> Ec2 parameters"},
                         "No param file");
        }
        catch(Exception x) {
            GuiUtil.exceptionMessage(x);
        }
   }

   public String getValue(String key) {
        return( get(key.toLowerCase()) );
   }

   public String setValue(String key, String value) {
       return( put(key.toLowerCase(),value) );
   }

       // --------------------------------------------
   
   public static String getDefaultConfigFilePath() {
        return( SysProp.user_home.getVal() + "/" + Constants.CONFIG_FILE_NAME );
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
         *  does nothing does not throw an exception.
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
                setValue(a[0],a[1]);
            }
        }
    }    
}
