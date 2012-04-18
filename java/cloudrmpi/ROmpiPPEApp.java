/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudrmpi;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;

/**
 *
 * @author Barnet Wagman
 */
public class ROmpiPPEApp extends PPEApp {       
    
    public static final String US_EAST_ENDPOINT = 
                                    "ec2.us-east-1.amazonaws.com";
    
    Region ec2Region;
    
    public ROmpiPPEApp(String[] argv) throws Exception {
        super(argv);      
        ec2Region = new Region();
        ec2Region.setEndpoint(US_EAST_ENDPOINT);
    }
    
    public void launchNetworkManager() {
        super.launchNetworkManager();
        ((ROmpiPPEManager) ppeManager).startCmdHandler();
    }

    protected PPEManager createPPEManager() throws Exception  {
        return(new ROmpiPPEManager(argv,
                                  ConstantsPPE.CLOUDRMPI_AMI_GROUP,
                                  ec2Region));
    }
    
    protected String getTitle() {
        return("cloudRmpi EC2 Network Manager");
    }        
    
    public static void main(String[] argv) throws Exception {
        ROmpiPPEApp app = new ROmpiPPEApp(argv);
        app.launchNetworkManager();        
    }
}
