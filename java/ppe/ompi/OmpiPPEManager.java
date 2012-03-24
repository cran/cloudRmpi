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

package ppe.ompi;

import ppe.*;
import ppe.ec2.*;
import ppe.gui.*;
import ppe.gui.networkspec.*;
import nbl.utilj.*;
import javax.swing.*;

/** A gui for launching and managing a network of Ec2 instances running
 *  ompi.  The {@link OmpiPPEManager#createNetwork() } method in this
 *  class configures ompi.
 *
 * @author Barnet Wagman
 */
public class OmpiPPEManager extends PPEManager {

    public static final String GUI_FRAME_TITLE = "ppe-ompi";

    public OmpiPPEManager(String[] argv,boolean shutdownAppOnExit) 
        throws Exception {
        super(argv,GUI_FRAME_TITLE,shutdownAppOnExit);
        ExceptionHandler.useGui = false;
    }
    
    /** Equivalent to <tt>OmpiPPEManager(argv,true)</tt>.
     * 
     * @param argv 
     */
    public OmpiPPEManager(String[] argv) throws Exception {
        this(argv,true);
    } 

    protected void specifyServices() {
        Services.setServicesClass(ppe.ompi.OmpiConfigServices.class);
    }

    protected void createNetwork() throws Exception {
        InProgressFrame.showInProgress();
        NetworkSpec nS = new NetworkSpec(paramsEc2);
        nS.networkName = ec2w.chooseNetworkName(paramsEc2);
        OmpiSpec oS = new OmpiSpec(new ParamsOmpi(paramHt,paramsEc2));

        NetworkSpecGui specGui = new NetworkSpecGui(ec2w,nS, oS);
        if ( !specGui.fillSpec() ) return;
        final NetworkSpec networkSpec = specGui.choicesToNetworkSpec();           
        if ( !networkSpec.isComplete() )
        throw new IncompleteNetworkSpecException(networkSpec.listMissingParams());
        final OmpiSpec specOmpi = specGui.choicesToOmpiSpec();

            // This will take a while, so put it in it's own thread.
        (new Thread() { public void run() {
            try {
                    // Create the network                    
                String networkID = ec2w.createNetwork(networkSpec);

                if ( !pingNetwork(networkID) ) {
                        // There are NO usable instance, so stop now.
                    GuiUtil.warning(
                            new String[] {
                                "There are NO usable instances.",
                             },
                             "No usable instances");
                    return;
                }

                NetworkInfo ni = NiM.getForID(networkID);
                if ( ni == null ) throw new RuntimeException("NO " +
                     " network info for ID=" + networkID);
                Services s = ni.getServices(); 
                s.set(ec2w, networkSpec, specOmpi);
                s.launch();
                NiM.update(ec2w);
            }
            catch(Exception xxx) {
                ExceptionHandler.gui(xxx);
                GuiUtil.warning(new String[] {
                    "   Ompi may not have been configured"
                    },
                    "Warning");                    
            }
         }}).start();

         JOptionPane.showMessageDialog(
            null,
            new String[] {
                "A request for ec2 instances has been submitted to AWS.",
                "See the ec2 network manager window for the network's status."
            },
            "ec2 network launch in progress",
            JOptionPane.PLAIN_MESSAGE);        
    }

    protected void rebootInstances(String networkID) throws Exception {
       NetworkInfo ni = NiM.getForID(networkID);
        if ( ni == null ) return;
        if ( GuiUtil.answerIsYes(
            new String[] { "Reboot all instances in network",
                           ni.getNetworkName() + " ?" },
            "Reboot instances") ) {
            ec2w.rebootInstances(ni);
            NiM.update(ec2w);
        }
    }
        
        
        // ........................................................

    public static void main(String[] argv) throws Exception {

        OmpiPPEManager m = new OmpiPPEManager(argv);
        m.launchGui();
    }
}
