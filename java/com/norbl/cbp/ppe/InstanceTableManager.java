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

import com.norbl.cbp.ppe.gui.*;
import javax.swing.*;
import java.util.*;

/** Holds {@link InstanceTableModel}s and their associated
 *  swing components. This allows us to add and remove
 *  tables from a tabbed pane as they go in and out of existence.
 * 
 *  While this class supports guis, it is tracks the status of instances.
 *  Hence it is in the ppe package rather than the ppe.gui package.
 *
 * @author Barnet Wagman
 */
public class InstanceTableManager implements StateChangeListener {

        /** Keyed by network ID
         * 
         */
    HashMap<String,Itm> ht;

    JTabbedPane tabbedPane;
    
    EbsVolumeWrangler ebsVolumeWrangler;   
       
    public InstanceTableManager(EbsVolumeWrangler ebsVolumeWrangler) {
        this.ebsVolumeWrangler = ebsVolumeWrangler;
        ht = new HashMap<String,Itm>();       
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void stateChanged() { update(); }

    public void update() {
//         /* D */ System.out.println("### InstanceTableMANAGER: update()");
        long updateTime = System.currentTimeMillis();

        List<NetworkInfo> nis = NiM.getNetworks();
        synchronized(nis) {
            for (NetworkInfo ni : nis ) {
                Itm itm = ht.get(ni.getNetworkID());

                 if ( (itm == null) // we need to create a new one.
                       &&
                      ( tabbedPane != null )
                    ) { // There's no need to create an itm if
                        // the tabbedPane isn't available yet.
                    itm = new Itm(ni);
                    ht.put(itm.ni.getNetworkID(),itm);
                    itm.createJComponents(tabbedPane);
                 }

                if ( itm != null ) {
                    itm.tableModel.stateChanged();
                    itm.updateTime = updateTime;
                }
            }
        }
//        /* D */ System.out.println("### InstanceTableMANAGER: n itms=" + ht.size());

            // Remove any tabs for networks that are gone
        List<String> deceasedNetworks = new ArrayList<String>();
        for ( Iterator<Itm> it = ht.values().iterator(); it.hasNext(); ) {
            Itm itm = it.next();
            if ( itm.updateTime < updateTime )
                deceasedNetworks.add(itm.ni.getNetworkID());
        }
        for (String nid : deceasedNetworks ) {
            Itm x = ht.remove(nid);
            if ( (tabbedPane != null) && (x.scrollPane != null) ) {
                x.removeFromTabbedPane(tabbedPane);
            }
        }
    }

        /** Holds an instance table model the ni and scroll pane
         *  associated with it.<p>.
         *
         *  Remember that the model retrieves its data from
         *  the NetworkInfo hashtable, so whenever
         *  {@link NetworkInfo#findNetworks(java.util.List)} is
         *  invoked the models are effectively updated.
         *
         */
    class Itm {

        NetworkInfo ni;
        InstanceTableModel tableModel;
        JScrollPane scrollPane;
        long updateTime;
        InstanceTablePopup itPopup;

        Itm(NetworkInfo ni) {
            updateTime = 0L;
            this.ni = ni;
            tableModel = new InstanceTableModel(ni);
            NiM.addStateChangeListener(tableModel);
        }

        void createJComponents(final JTabbedPane tabbedPane) {
            java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
                scrollPane = new JScrollPane();
                JTable jt = new JTable();
                jt.setModel(tableModel);
                jt.setCellSelectionEnabled(true);
                scrollPane.setViewportView(jt);
                tabbedPane.addTab(ni.getNetworkName(),scrollPane);
                if ( ebsVolumeWrangler != null )
                    itPopup = new InstanceTablePopup(jt, tableModel,
                                                     ebsVolumeWrangler);
                        
            }});
        }

        void removeFromTabbedPane(final JTabbedPane tabbedPane) {
            java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
            tabbedPane.remove(scrollPane);
            scrollPane = null;
            }});
        }
    }
    
    public void setEbsVolumeWrangler(EbsVolumeWrangler ebsVolumeWrangler) {
        this.ebsVolumeWrangler = ebsVolumeWrangler;
    }
}
