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

import java.util.*;
import java.io.*;
import com.norbl.util.gui.*;


/** Base class for constructing subsets of the params in {@link ParamHt}.
 *
 * @author Barnet Wagman
 */
abstract public class AbstractParams {

    protected ParamHt ht;

    public AbstractParams(ParamHt ht) throws Exception {
        this.ht = ht;        
    }
    
    protected void setParamVals() throws Exception {

        for ( Iterator<String> it = ht.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            String val = ht.get(key);
            setParamVal(key,val);
        }
    }

    abstract protected void setParamVal(String key, String val)
        throws Exception;


     protected String getConfigFileSuffix() {

        String configFile = ht.get(ParamHt.ParamName.configFile.toString());

        String cfs;
        if ( configFile != null )
            cfs = "or in config file " + configFile;
        else cfs = "and the default config file " +
                    ParamHt.getDefaultConfigFilePath(ht.configFilenameDefault) +
                   " does not exist.";
        return(cfs);
    }

    static public boolean checkForReadFileAccess(File f) {
//        throws InaccessibleFileException {
        if ( f.exists() && !f.isDirectory() && f.canRead() )
            return(true);
        else { 
            String mess;     
            if ( f.exists() ) {                     
                if ( f.isDirectory() ) mess = "is not a key pair file.";
                else if ( !f.canRead() ) mess = "exists but is not accessible.";
                else mess = "cannot be accessed.";
            }
            else mess = "does not exists.";
            GuiUtil.warning(new String[] {
                "RSA key pair file " + f.getPath(),
                mess,
                "You'll need to correct this before you can launch instances."
                },
                "Key pair file problem"
            );
            return(false);
        }      
    }
}
