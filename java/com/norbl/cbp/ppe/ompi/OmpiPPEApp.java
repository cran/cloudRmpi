/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.ompi;

import com.norbl.cbp.ppe.*;
import com.amazonaws.services.ec2.model.*;


/** 
 *
 * @author Barnet Wagman
 */
public class OmpiPPEApp extends PPEApp {       
    
    public static final String US_EAST_ENDPOINT = 
                                    "ec2.us-east-1.amazonaws.com";

    Region ec2Region;
    
    public OmpiPPEApp(String[] argv) throws Exception {
        super(argv);      
        ec2Region = new Region();
        ec2Region.setEndpoint(US_EAST_ENDPOINT);
    }
    
    protected PPEManager createPPEManager() throws Exception  {
        return(new OmpiPPEManager(argv,
                                  ConstantsPPE.OMPI_AMI_GROUP,
                                  ec2Region));
    }
    
    protected String getTitle() {
        return("Open MPI EC2 Network Manager");
    }
        
    public static void main(String[] argv) throws Exception {
        OmpiPPEApp app = new OmpiPPEApp(argv);
        app.launchNetworkManager();
    }
}
