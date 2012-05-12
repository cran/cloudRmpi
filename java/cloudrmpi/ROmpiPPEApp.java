/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudrmpi;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;

/** This app is used to launch an instance of the {@link ROmpiPPEManager}.  
 *  It is normally launched by a the ppe.launchNetworkManager() R function.<p>
 * 
 *  <b>Ports</b><br>
 *  This app communicates with the local R session using a port specified in
 *  argv.  By default it is port <b>4461</b>.  Note that rreval usually uses
 *  ports <b>4460</b> and <b>4464</b> (see {@link rreval.RReClientApp} for details).
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
