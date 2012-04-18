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

package com.norbl.cbp.ppe;

import com.norbl.cbp.ppe.usermonitor.*;
import com.norbl.util.*;
import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.model.*;
import com.norbl.util.gui.*;

/**
 *
 * @author Barnet Wagman
 */
public class ParamsEc2 extends AbstractParams {

    public enum ParamName {
        awsAccessKey,
        awsSecretKey,
        awsUserID,
        instanceType,
        imageID,
        nInstances,
        keyName,
        networkName,    
        useSpotInstances,
        spotPrice,
        useCluster,
        clusterGroupName,
        disableHyperthreading,       
        securityGroup,
        rsaKeyPairFile,
        uid
        ;

        public static ParamName forKey(String key) {
            try {
                return(ParamName.valueOf(key));
            }
            catch(IllegalArgumentException iax) {
                    // Look for a caseless match.  This is to support old
                    // config files
                for ( ParamName x : ParamName.values() ) {
                    if ( x.toString().toLowerCase().equals(key.toLowerCase()) )
                        return(x);
                }
                return(null);
            }
        }
    }

    private String awsAccessKey;
    private String awsSecretKey;

        /** On the AWS security credentials page, this is referred to as
         *  the "Account number" and as the "AWS Account ID.  However,
         *  in this context it is used without hyphens.
         */
    private String awsUserID;

    public InstanceType instanceType;
    public String imageID;
    public int nInstances;
    public String keyName;

        /** This keypair file must contain the PRIVATE key that matches
         *  the public key specified by keyName (which is registered with
         *  aws).
         */
    public File rsaKeyPairFile;

        /** Identifies a set of ec2 instances that constitute an mpi
         *  network. By default this is &lt;user name&gt;-&lt;date&gt;, e.g.
         *
         *  <blockquote><tt>
         *  username-20110221
         *  </blockquote></tt>
         *
         *  but it can be anything.  This is just for convenience. Typically
         *  it's a string that's easier to type (and remember) than the
         *  {@link #networkID}.  It does not need to be unique.
         */
    public String networkName;
    public boolean useSpotInstances;
    public double spotPrice;
    public boolean useCluster;
    public String clusterGroupName;
    public boolean disableHyperthreading;  
    public String securityGroup;
    public String uid;  
    
    
    
    public ParamsEc2(ParamHt ht) throws Exception {
        super(ht);

            // Set default values
        useSpotInstances = false;
        spotPrice = Double.NaN;
        useCluster = false;
        disableHyperthreading = true;
        nInstances = -1;

        setParamVals();
        checkParamConsistency();
    }
    
    protected void setParamVal(String key, String val) throws Exception {

        String value;
        if ( val != null ) value = val.trim();
        else value = null;
       
        ParamName nm = ParamName.forKey(key);
       
        if ( nm == null ) return; // Unknown names are ignored.

        switch(nm) {
            case awsAccessKey:
                awsAccessKey = value;
                break;
            case awsSecretKey:
                awsSecretKey = value;
                break;
            case awsUserID:
                awsUserID = value;
                break;
            case instanceType:                
                instanceType = Ec2InstanceType.getInstanceType(value);                
                break;
            case imageID:
                imageID = value;
                break;
            case nInstances:
                nInstances = Integer.parseInt(value);
                break;
            case keyName:
                keyName = value;
                break;
            case networkName:
                networkName = value;
                break;                      
            case useSpotInstances:
                if ( StringUtil.isValidBooleanFlag(value) )
                    useSpotInstances = Boolean.parseBoolean(value);
                else throw new ImproperParamException("If specified, " +
                        "useSpotInstances=<true|false>; it's value was=" +
                        value);
                break;
            case spotPrice:
                spotPrice = Double.parseDouble(value);
                break;
            case useCluster:
                if ( StringUtil.isValidBooleanFlag(value) )
                    useCluster = Boolean.parseBoolean(value);
                else throw new ImproperParamException("If specified, " +
                        "useCluster=<true|false>; it's value was=" +
                        value);
            case clusterGroupName:
                clusterGroupName = value;
                break;
            case disableHyperthreading:
                if ( StringUtil.isValidBooleanFlag(value) )
                    disableHyperthreading = Boolean.parseBoolean(value);
                else throw new ImproperParamException("If specified, " +
                        "disableHyperthreading=<true|false>; it's value was=" +
                        value);              
            case securityGroup:
                securityGroup = value;
                break;
            case rsaKeyPairFile:
                rsaKeyPairFile = new File(value);
                checkForReadFileAccess(rsaKeyPairFile);
                break;
            case uid:
                uid = value;               
                break;    
            default:
        }        
    }

    public AWSCredentials buildAWSCredentials()
        throws MissingParamsException {

        return(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }
    
    public String getAwsAccessKey() { return(awsAccessKey); }
    public String getAwsSecretKey() { return(awsSecretKey); }

    public String getAWSUserID() { return(awsUserID); }

    public String getMissingParamMessage() {
        if ( ht.configFile != null )
            return( " not in argv or config file " + ht.configFile.getPath() );
        else return( " not in argv and the default config file " +
                     ht.getDefaultConfigFilePath(ht.configFilenameDefault) + " does not exist." );

    }

        // ---------------------------------------------
    
    private void checkParamConsistency() throws ImproperParamException {

        if ( useSpotInstances && useCluster )
            throw new ImproperParamException("Both useSpotInstances and " +
                " useCluster are set true.  AWS does not clusters with " +
                " spot instances.");

        if ( useSpotInstances && (spotPrice <= 0.0) )
            throw new ImproperParamException("Use spot instances is set to " +
                "true but the spot price=" + spotPrice + " which is invalid.");

    }

        // ------ Missing param tests ------------------
   
    public void checkForConnectToAWSParams()
        throws MissingParamsException {
        if ( (awsAccessKey == null) || (awsSecretKey == null) ||
             (awsUserID == null) ||
             (instanceType == null) || (imageID == null) ||
             (nInstances < 1) || (keyName == null)) {

            String cfs = getConfigFileSuffix();

            String s = "";
            if ( awsAccessKey == null )
                s += "The required param awsAccessKey was not specified in argv " +
                      cfs;
            if ( awsSecretKey == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsSecretKey was not specified in argv " +
                      cfs;
            }
            if ( awsUserID == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsUserID was not specified in argv " +
                      cfs;
            }
        }
    }

        /** Checks for required params, not for params that
         *  may be supplied though a gui.
         * 
         * @throws MissingParamsException
         */
    public void checkForCreateNetworkParams()
        throws MissingParamsException {
        if ( (awsAccessKey == null) || (awsSecretKey == null) ||
             (awsUserID == null) || 
             (instanceType == null) || (imageID == null) ||
             (nInstances < 1) || (keyName == null)) {

            String cfs = getConfigFileSuffix();

            String s = "";
            if ( awsAccessKey == null )
                s += "The required param awsAccessKey was not specified in argv " +
                      cfs;
            if ( awsSecretKey == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsSecretKey was not specified in argv " +
                      cfs;
            }
            if ( awsUserID == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsUserID was not specified in argv " +
                      cfs;
            }
            throw new MissingParamsException(s);
        }
    }

    public void checkForNetworkInfoParams()
        throws MissingParamsException {
        if ( (awsAccessKey == null) || (awsSecretKey == null) ||
             (awsUserID == null) ) {
            String cfs = "";
            
            String s = "";
            if ( awsAccessKey == null )
                s += "The required param awsAccessKey was not specified in argv " +
                      cfs;
            if ( awsSecretKey == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsSecretKey was not specified in argv " +
                      cfs;
            }
            if ( awsUserID == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsUserID was not specified in argv " +
                      cfs;
            }
            throw new MissingParamsException(s);
        }
    }

     public void checkForTerminateInstancesParams()
        throws MissingParamsException {
        if ( (awsAccessKey == null) || (awsSecretKey == null) ||
             (awsUserID == null) ) {
            String cfs = getConfigFileSuffix();
            String s = "";
            if ( awsAccessKey == null )
                s += "The required param awsAccessKey was not specified in argv " +
                      cfs;
            if ( awsSecretKey == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsSecretKey was not specified in argv " +
                      cfs;
            }
            if ( awsUserID == null ) {
                if ( s.length() > 0 ) s += "\n";
                s += "The required param awsUserID was not specified in argv " +
                      cfs;
            }

            throw new MissingParamsException(s);
        }
    }   
     
     /** Checks whether this instance has the params needed to create
      *  {@link Ec2Wrangler} and launch instances.  These params
      *  are usually obtained from the config file.<p>
      * 
      *  Note that this methods does not check for params needed
      *  to configure ompi (e.g. rsaKeyPairFile) nor to check
      *  for billable ami authorization.
      * 
      * @return 
      */
    public boolean hasRequiredEc2LaunchParams() {
        return( (awsUserID != null) &&
                (awsAccessKey != null) && (awsSecretKey != null) &&                
                (keyName != null) );
    }
    
    public boolean hasNBLAuthorizationParams() {
        try {
            return( uid != null );                     
        }
        catch(Exception xxx) { 
            GuiUtil.exceptionMessage(xxx);
            return(false);
        }
    }     
}
