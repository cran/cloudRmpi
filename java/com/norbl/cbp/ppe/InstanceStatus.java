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

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;

/** A wrapper for {@link Instance}.  Note that {@link Instance}s
 *  are obtained using {@link AmazonEC2#describeInstances()}. It
 *  represents the status of an instance and is not used to control it.
 *
 * @author Barnet Wagman
 *
 */
public class InstanceStatus {
    
    public Instance instance;
    public List<String> securityGroupNames;

    public InstanceStatus(Instance instance, List<String> securityGroupNames) {
        this.instance = instance;
        this.securityGroupNames = securityGroupNames;
    }

    public boolean equals(Object other) {
        if ( !(other instanceof InstanceStatus) ) return(false);
        String dn = this.getPublicDnsName();
        String odn = ((InstanceStatus) other).getPublicDnsName();
        if ( (dn != null) && (odn != null) )
            return( dn.equals(odn) );
        else if ( (dn == null) && (odn == null) ) return(true);
        else return(false);
//        return( this.getPublicDnsName().equals(
//                ((InstanceStatus) other).getPublicDnsName()) );
    }
    
    /** This method covers a deficiency in the aws api.  It uses
     * Instance.setInstanceType(instanceType) but Instance.getInstanceType()
     * returns a string.
     * @return 
     */
    public InstanceType getInstanceType() {
        return(Ec2InstanceType.getInstanceType(instance.getInstanceType()));
    }

    public String getTagValue(InstanceTag tagType) {

        String tt = tagType.toString();
        List<Tag> tags = instance.getTags();

        for ( Tag t : tags ) {
            if ( t.getKey().equals(tt) ) return(t.getValue());
        }
        return(null);
    }

    public String getPublicDnsName() {
        return( instance.getPublicDnsName() ); /* D */       
    }

    public String getNetworkID() {
        return( getTagValue(InstanceTag.networkID) );
    }

    public String getNetworkName() {
        return( getTagValue(InstanceTag.networkName) );
    }

    public boolean isMaster() {
        String nt = getTagValue(InstanceTag.nodeType);
        return( NodeType.valueOf(nt).equals(NodeType.master) );
    }

    public boolean isSlave() {
        String nt = getTagValue(InstanceTag.nodeType);
        return( NodeType.valueOf(nt).equals(NodeType.slave) );
    }
    
    public String getNodeType() {
        return(getTagValue(InstanceTag.nodeType));
    }

        /**
         *
         * @return true if state == running or pending, else false.
         */
    public boolean isActive() {
        String s = instance.getState().getName();
        return( s.equals("running") || s.equals("pending") );
    }

        /**
         *
         * @return true if state == running
         */
    public boolean isRunning() {
        String s = instance.getState().getName();
        return( s.equals("running")  );
    }

    public static boolean isRunning(Instance ins) {
        if ( ins != null )
            return( "running".equals(ins.getState().getName()) );
        else return(false);
    }

    public boolean hasNetworkID(String networkID) {
        String nid = getNetworkID();
        if ( nid != null ) return( nid.equals(networkID) );
        else return(false);
    }

    public String getSummary() {
        return(instance.getInstanceId() + " " +
               instance.getInstanceType() + " " +
               instance.getImageId() + " " +
               instance.getPublicDnsName() + " " +
               instance.getLaunchTime().toString() + " " +
               getNetworkName() + " " + getNetworkID() + " " +
               getTagValue(InstanceTag.nodeType) + " " +
               instance.getState().getName()
               );
    }
}
