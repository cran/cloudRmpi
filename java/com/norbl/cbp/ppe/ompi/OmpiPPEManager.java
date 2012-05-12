/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.ompi;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;
import com.norbl.cbp.ppe.gui.networkspec.*;

/**
 *
 * @author Barnet Wagman
 */
public class OmpiPPEManager extends PPEManager {

    public static final Class AMI_CHOICE_CLASS = AmiChoiceU.class;
    public static final Class NSG_FRAME_CLASS = NSGFrameU.class;
    
    ParamsOmpi paramsOmpi;
    OmpiSpec ompiSpec;
    
    public OmpiPPEManager(String[] argv, 
                          String amiGroup,
                          Region ec2Region) throws Exception {
        super(argv,amiGroup,ec2Region);
    }
    
    protected void specifyServices() {
        Services.setServicesClass(OmpiConfigServices.class);
    }
        
    protected void initializeServices(NetworkSpec networkSpec,
                                      Services services) 
        throws Exception {
        
        paramsOmpi = new ParamsOmpi(paramHt, paramsEc2);
        
        ompiSpec = new OmpiSpec(new ParamsOmpi(paramHt,paramsEc2));
        ompiSpec.networkName = networkSpec.networkName;        
        ompiSpec.instanceType = networkSpec.instanceType;
        Ec2InstanceType eit = 
            Ec2InstanceType.getEc2InstanceType(instanceTypes,
                                               networkSpec.instanceType);
        ompiSpec.slotsPerHost = eit.nCores;
                               
        if ( ompiSpec.isComplete() ) {        
            ((OmpiConfigServices) services).set(ec2w);
//            ((OmpiConfigServices) services).set(ompiSpec);   
            ((OmpiConfigServices) services).set(paramsEc2);
            ((OmpiConfigServices) services).set(networkSpec);
        }
        else throw new MissingParamsException("OmpiSpec is not complete:" +
                                              ompiSpec.listMissingParams());             
    }
    
    /**
     * 
     * @param networkName
     * @return A fully specified network spec or null if the user cancels.
     */
    protected NetworkSpec getFullySpecifiedNetworkSpec(String networkName) {
        try {
            NetworkSpecGui nsg = new NetworkSpecGui(this,
                                                    AMI_CHOICE_CLASS,
                                                    NSG_FRAME_CLASS,
                                                    networkName);
            if ( nsg.fillSpec() ) return(nsg.choicesToNetworkSpec());
            else return(null);        
        }
        catch(Exception xxx) {
            GuiUtil.exceptionMessage(xxx);
            return(null);
        }
    }     
    
    protected String getAboutAppTitle() { return("ppe-ompi"); }
    protected String getAboutAppVersion() { 
        return("<html>" + 
        "Version 1.2<br><br><br> &copy; 2012, Barnet Wagman<br>Northbranchlogic, Inc." +
                "<html>"
              );
    }
    
    protected String getAmiWebpageUrl() {
        return("https://s3.amazonaws.com/norbl/Machine_images_ppe-ompi.html");
    }
    
    protected String getManualWebpageUrl() {
        return("http://norbl.com/ppe-ompi/ppe-ompi.html");
    }
}
