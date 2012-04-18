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

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;

/** Contains all variables needed to specify an ec2 network.
 *  Note that this does not include the aws credentials (which
 *  are used to create the <tt>AmazonEC2Client</tt>).<p>
 * 
 *  The NetworkSpec also includes userData (from spec.uid) which is
 *  optional.
 *
 * @author Barnet Wagman
 */
 public class NetworkSpec implements java.io.Serializable {

    static final long serialVersionUID = ConstantsPPE.SERIAL_VERSION_UID;

    public String networkName;
    public InstanceType instanceType;
    public String imageID;
    public int nInstances;
    public String keyName;
    public String securityGroupName;
    public boolean useSpotInstances;
    public double spotPrice;
    public String clusterGroupName;
    public boolean useCluster;
    public int slotsPerHost;
    public boolean disableHyperThreading;
    public String availabilityZone;
    public String userData;

    /** Note that the userData is taken from params.uid which
     *  may be null.
     * 
     * @param params 
     */
    public NetworkSpec(ParamsEc2 params) {
        networkName = params.networkName;
        instanceType = params.instanceType;
        availabilityZone = null;
        nInstances = params.nInstances;
        keyName = params.keyName;
        securityGroupName = params.securityGroup;
        useSpotInstances = params.useSpotInstances;
        spotPrice = params.spotPrice;
        clusterGroupName = params.clusterGroupName;
        useCluster = params.useCluster;
        disableHyperThreading = params.disableHyperthreading;
        userData = params.uid;
    }

    public NetworkSpec() {}

    public boolean isComplete() {
        return( (networkName != null) &&
                (instanceType != null) &&
                (imageID != null) &&
                (nInstances > 0) &&
                (keyName != null) &&                
                (useSpotInstances?!Double.isNaN(spotPrice):true) &&
                (useCluster?(clusterGroupName != null):true) &&
                (slotsPerHost > 0)
        );
    }

    public String listMissingParams() {
        return("Missing params: " +
                ((networkName == null)?"networkName ":"") +
                ((instanceType == null)?"instanceType ":"") +
                ((imageID == null)?"imageID ":"") +
                ((nInstances <= 0)?"nInstances ":"") +
                ((keyName == null)?"keyNames ":"") +               
                ( (useSpotInstances?Double.isNaN(spotPrice):true)
                  ?"spotPrice":"") +
                ( (useCluster?(clusterGroupName == null):true)
                  ?"clusterGroupName":"")  +
                ((slotsPerHost < 1)?"slotsPerHost":"")
               );
    }

    public NetworkSpec cloneSpec() {
        NetworkSpec c = new NetworkSpec();
        c.networkName = nns(this.networkName);
        c.instanceType = this.instanceType; // nns(this.instanceType);
        c.nInstances = this.nInstances;
        c.keyName = nns(this.keyName);
        c.securityGroupName = nns(this.securityGroupName);
        c.useSpotInstances = this.useSpotInstances;
        c.slotsPerHost = this.slotsPerHost;
        c.spotPrice = this.spotPrice;
        c.clusterGroupName = nns(this.clusterGroupName);
        c.useCluster = this.useCluster;
        c.disableHyperThreading = this.disableHyperThreading;
        c.userData = this.userData;
        return(c);
    }

    private String nns(String x) {
        if ( x != null ) return(new String(x));
        else return(null);
    }
}
