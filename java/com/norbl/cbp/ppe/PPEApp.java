/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.norbl.cbp.ppe.gui.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;

/** Concrete extensions of this class launch the network manager app.
 *  It handles creating the {@link PPEManager} and the network manager
 *  JFrame.
 *
 * @author Barnet Wagman
 */
abstract public class PPEApp {
    
    protected String argv[];
    protected PPEManager ppeManager;
    NetworkManagerFrame nmFrame;
    
    private static boolean VERBOSE = false;
    
    public PPEApp(String[] argv) throws Exception  {
        this.argv = argv;
        System.setProperty("org.apache.commons.logging.Log",
                           "com.norbl.util.NearNilLog");
        GuiMetrics.init();
        SwingDefaults.setDefaults();        
    }
    
    public void launchNetworkManager() {
        try {
            InProgressFrame.showInProgress();
            ppeManager = createPPEManager();
            ppeManager.setupAWSWrangling();    
            launchGui();
        }
        catch(Exception xxx) {
            InProgressFrame.end();
            ExceptionHandler.gui(xxx);
        }
        finally { InProgressFrame.end(); }
    }
    
    protected void launchGui() {        
       
        nmFrame = new NetworkManagerFrame(ppeManager, getTitle());
       
        try {            
            java.awt.EventQueue.invokeAndWait(
                new Runnable() { public void run() {
                    nmFrame.startGui();
                    (new Thread() { public void run() {
                    if ( !ppeManager.paramsEc2.hasRequiredEc2LaunchParams() ) {                  
                        MessageDialog.showNotGoodToGoWarning(nmFrame, ppeManager);
                    }
                }}).start();
            }});
            InProgressFrame.end();              
        }
        catch(Exception xxx) { {
            InProgressFrame.end();            
            ExceptionHandler.gui(xxx);
        } }
        
    }

    abstract protected PPEManager createPPEManager() throws Exception ;
    abstract protected String getTitle();
    
    public static void verbose(String s) {
        if ( VERBOSE ) System.out.println(s); 
    }
    public static void verbose(Throwable s) {
        if ( VERBOSE ) System.out.println(s); 
    }
}
