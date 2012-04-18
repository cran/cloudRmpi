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

package com.norbl.cbp.ppe.ompi;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import java.util.*;
import java.io.*;
import com.amazonaws.services.ec2.model.*;

/** Parameters used to configure an open-mpi network.
 *
 * @author Barnet Wagman
 */
public class ParamsOmpi {

    public enum ParamName {        
        networkName,
        rsaKeyPairFile,
        slotsPerHost,
        disableHyperthreading,
        instanceType;

//        String key;
//        ParamName() {
//            key = this.toString().toLowerCase();
//        }

//        static ParamName valueForKey(String key) {
//            for ( ParamName pn : ParamName.values() ) {
//                if ( pn.key.equals(key) ) return(pn);
//            }
//            return(null);
//        }
    }

    public String networkName;

        /** This keypair file must contain the PRIVATE key that matches
         *  the public key specified by keyName (which is registered with
         *  aws).
         */
    public File rsaKeyPairFile;

        /** The number of 'process slots' to be created on each host,
         *  most commonly one per core.
         */
    public int slotsPerHost;

    public boolean disableHyperthreading;
    public InstanceType instanceType;

    ParamHt ht;

    public ParamsOmpi(ParamHt ht,ParamsEc2 paramsEc2)
        throws InaccessibleFileException, ImproperParamException,
               MissingParamsException {
        
        this.ht = ht;

            // Set params that are NOT derived from paramsEc2
        setParamVals();

            // Copy params from paramsEc2
        this.networkName = paramsEc2.networkName;
        this.disableHyperthreading = paramsEc2.disableHyperthreading;
        this.instanceType = paramsEc2.instanceType;


        checkForAccessInstanceParams();
    }

    public void setParamVals()
        throws InaccessibleFileException, ImproperParamException {

        for ( Iterator<String> it = ht.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            String val = ht.get(key);           
            setParamVal(key,val);            
        }
    }

    public void setParamVal(String key, String val)
        throws InaccessibleFileException, ImproperParamException {

        String value = val.trim();
     
        try {
            ParamName nm = ParamsOmpi.ParamName.valueOf(key);
         
            switch(nm) {           
                case rsaKeyPairFile:
                    rsaKeyPairFile = new File(value);
                    checkForReadFileAccess(rsaKeyPairFile);
                    break;
                case slotsPerHost:
                    slotsPerHost = Integer.parseInt(value);
                    break;             
                default:
            }
        }
        catch(IllegalArgumentException iax) {}// Unknown names are ignored.
    }

         // ---------------------------------------------

    private void checkForReadFileAccess(File f)
        throws InaccessibleFileException {
        if ( f.exists() && !f.isDirectory() && f.canRead() )
            return;
        else throw new InaccessibleFileException(
                "RSA key pair file " + f.getPath() +
                " is not accessible: exists=" + f.exists() +
                "  is file=" + !f.isDirectory() +
                "  can read=" + f.canRead());
    }

    public void checkForAccessInstanceParams()
        throws MissingParamsException {
        String cfs = getConfigFileSuffix();
        String s = "";
        if ( rsaKeyPairFile == null ) {
            if ( s.length() > 0 ) s += "rsaKeyPairFile ";
            s += "The required param rsaKeyPairFile was not specified in argv " +
                  cfs;
            throw new MissingParamsException(s);
        }
    }

    private String getConfigFileSuffix() {

        String configFile = ht.get(ParamHt.ParamName.configFile.toString());

        String cfs;
        if ( configFile != null )
            cfs = "or in config file " + configFile;
        else cfs = "and the default config file " +
                    ParamHt.getDefaultConfigFilePath(ht.configFilenameDefault) +
                   " does not exist.";
        return(cfs);
    }
}
