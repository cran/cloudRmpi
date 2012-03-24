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

package ppe.ec2;

import ppe.*;
import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.model.*;

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
        rsaKeyPairFile,;

        String key;
        ParamName() {
            key = this.toString().toLowerCase();
        }

        static ParamName valueForKey(String key) {
            for ( ParamName pn : ParamName.values() ) {
                if ( pn.key.equals(key) ) return(pn);
            }
            return(null);
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

        String value = val.trim();

        ParamName nm = ParamName.valueForKey(key);
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
                instanceType = Ec2InstanceType.getInstanceType(value); //value;                
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
                if ( UtilPPE.isValidBooleanFlag(value) )
                    useSpotInstances = Boolean.parseBoolean(value);
                else throw new ImproperParamException("If specified, " +
                        "useSpotInstances=<true|false>; it's value was=" +
                        value);
                break;
            case spotPrice:
                spotPrice = Double.parseDouble(value);
                break;
            case useCluster:
                if ( UtilPPE.isValidBooleanFlag(value) )
                    useCluster = Boolean.parseBoolean(value);
                else throw new ImproperParamException("If specified, " +
                        "useCluster=<true|false>; it's value was=" +
                        value);
            case clusterGroupName:
                clusterGroupName = value;
                break;
            case disableHyperthreading:
                if ( UtilPPE.isValidBooleanFlag(value) )
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
            default:
        }        
    }

    public AWSCredentials buildAWSCredentials()
        throws ParamMissingException {

        return(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    public String getAWSUserID() { return(awsUserID); }

    public String getMissingParamMessage() {
        if ( ht.configFile != null )
            return( " not in argv or config file " + ht.configFile.getPath() );
        else return( " not in argv and the default config file " +
                     ht.getDefaultConfigFilePath() + " does not exist." );

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
        throws ParamMissingException {
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
         * @throws ParamMissingException
         */
    public void checkForCreateNetworkParams()
        throws ParamMissingException {
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
            throw new ParamMissingException(s);
        }
    }

    public void checkForNetworkInfoParams()
        throws ParamMissingException {
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
            throw new ParamMissingException(s);
        }
    }

     public void checkForTerminateInstancesParams()
        throws ParamMissingException {
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

            throw new ParamMissingException(s);
        }
    }   
     
     /** Checks whether the params that noramlly come from the config files
      *  exist.  These are the params needed to launch and access instances.
      * 
      * @return 
      */
    public boolean hasRequiredParams() {
        return( (awsUserID != null) &&
                (awsAccessKey != null) && (awsSecretKey != null) &&                
                (keyName != null) && (rsaKeyPairFile != null) );
    }
}
