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

package ppe.gui;

import ppe.*;
import ppe.ec2.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** Create the popup menu for a network (a line in the network table).
 *  This method examines the state to deterimine which menu options
 *  should be enabled.
 *
 * @author Barnet Wagman
 */
public class NetworkTablePopup {
    
    PPEManager ppeManager;
    JTable table;
    NetworkTableModel model;

    int networkNameCol;

    ClickHandler clickHandler;

    NetworkInfo ni;
    List<MiSpec> miSpecs;

    public NetworkTablePopup(PPEManager ppeManager, JTable table) {
        this.table = table;
        this.ppeManager = ppeManager;
        model = (NetworkTableModel) table.getModel();
        networkNameCol = model.getNetworkNameColumnIndex();
        clickHandler = new ClickHandler();
        table.addMouseListener(clickHandler);        
    }

    class ClickHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {         
            (new CreatePopup(e)).start();
        }
    }

        /** Before we can create the popup, we need to check
         *  states to determine which items should be enabled.
         *  This can entail operations that should not be on
         *  the awt event dispatching thread, hence this Thread class.                 
         */
    class CreatePopup extends Thread {

        MouseEvent e;
        int idx;
        boolean rebootInstances, terminateInstances,
                startRmxServerOnMaster,
                configureOmpi, startRserveController;
        
        CreatePopup(MouseEvent e) {
            this.e = e;            
        }

        public void run() {
            try {
                int idx = table.rowAtPoint(e.getPoint());
                if ( idx < 0 ) return;

                Object x = model.getValueAt(idx,networkNameCol);
                if ( x == null ) return;
                String networkName = (String) x;
                ni = NiM.getForName(networkName);
                if ( ni == null ) return;

                miSpecs = new ArrayList<MiSpec>();
               
                for ( PPEManager.Cmd cmd : PPEManager.Cmd.values() ) {
                    miSpecs.add(new MiSpec(cmd));
                }

                    // Now we can create the popup in the awt event dispatching thread
                java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    createPopup(e);
                }});
            }
            catch(Exception xxx) { ExceptionHandler.gui(xxx); }
        }
        
    }

    void createPopup(MouseEvent e) {

        JPopupMenu menu = new JPopupMenu(ni.getNetworkName());

        for (MiSpec mis : miSpecs ) {
            if ( mis.cmd.isNetworkCmd() )
                menu.add(mis.createMenuItem());
        }
        menu.show(e.getComponent(),e.getX(),e.getY());
    }

        // ----------------------------------


    private class MiSpec {
        PPEManager.Cmd cmd;
        boolean enable;
        ActionCommand ac;
        String txt;

        MiSpec(PPEManager.Cmd cmd) {
//            /* D */ System.out.println("NetworkTablePopup cmd=" + cmd);
            this.cmd = cmd;
            ac = new ActionCommand(cmd.toString(),
                                   ni.getNetworkID(),
                                   null);
            txt = cmd.getTitle();
            enable = ni.isRunning();
        }

        JMenuItem createMenuItem() {
            JMenuItem mi = new JMenuItem();
            mi.setActionCommand(ac.toActionEventString());
            mi.setText(txt);
//            if ( toolTip != null ) mi.setToolTipText(toolTip);
            mi.addActionListener(ppeManager);
            mi.setEnabled(enable);
//            /* D */ System.out.println("NetworkTablePopup: created mi for " +
//                    cmd.toString() + " " + txt);
            return(mi);
        }
    }
}
