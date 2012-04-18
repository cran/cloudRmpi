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
import java.io.*;
import com.amazonaws.services.ec2.model.*;

/**
 *
 * @author Barnet Wagman
 */
public class OmpiSpec implements java.io.Serializable {

    static final long serialVersionUID = ConstantsPPE.SERIAL_VERSION_UID;

    public String networkName;
    public File rsaKeyPairFile;
    public int slotsPerHost;
    public boolean disableHyperthreading;
    public InstanceType instanceType;

    public OmpiSpec(ParamsOmpi params) {
        this.networkName = params.networkName;
        this.rsaKeyPairFile = params.rsaKeyPairFile;
        this.slotsPerHost = params.slotsPerHost;
        this.disableHyperthreading = params.disableHyperthreading;
        this.instanceType = params.instanceType;
    }

    public OmpiSpec() {}

    public OmpiSpec(ParamsOmpi params, NetworkInfo ni) {
        this(params);

        this.networkName = ni.getNetworkName();
        if ( ni.instances == null ) return;
        if ( ni.instances.size() < 1 ) return;
        com.norbl.cbp.ppe.InstanceStatus s0 = ni.instances.get(0);
        this.instanceType =  s0.getInstanceType();
    }

    public boolean isComplete() {
        return( (networkName != null) &&
                (rsaKeyPairFile != null) &&
                (slotsPerHost > 0) &&
                (instanceType != null) );
    }

    public String listMissingParams() {
        return("Missing params: " +
                ((networkName == null)?"networkName ":"") +
                ((rsaKeyPairFile == null)?"rsaKeyPairFile ":"") +
                ((slotsPerHost <= 0)?"slotsPeHost ":"") +
                ((instanceType == null)?"instanceType ":"")
                );
    }

    public OmpiSpec cloneSpec() {
        OmpiSpec c = new OmpiSpec();
        c.networkName = nns(this.networkName);
        c.rsaKeyPairFile = (this.rsaKeyPairFile != null)
                            ?new File(this.rsaKeyPairFile.getPath())
                            :null;
        c.slotsPerHost = this.slotsPerHost;
        c.disableHyperthreading = this.disableHyperthreading;
        c.instanceType = this.instanceType; // nns(this.instanceType);
        return(c);
    }

    private String nns(String x) {
        if ( x != null ) return(new String(x));
        else return(null);
    }
}
