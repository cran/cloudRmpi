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
import javax.swing.table.*;
import ppe.util.*;

/** The Swing table model supporting info tables. All information
 *  in this model comes from {@link NetworkInfo} which maintains static
 *  hts of {@link NetworkInfo} objects.
 *
 * @author Barnet Wagman
 */
public class InstanceTableModel extends AbstractTableModel
   implements StateChangeListener {

    private enum ItColumn {

        publicDns("Public DNS"),
        status("Status"),
        nodeType("Node type"),
        launchTime("Launch time"),
        instanceType("Instance type"),
        ami("ami"),
        virualizationType("Virtualization");

        String nm;
        ItColumn(String nm) { this.nm = nm; }
    }

    public String networkID;
    public String networkName;
   
    public InstanceTableModel(NetworkInfo ni) {
        networkName = ni.getNetworkName();
        networkID = ni.getNetworkID();      
    }

    public int getRowCount() {
        NetworkInfo ni = getNetwork();
        if ( ni == null ) return(0);
        else return( ni.instances.size() );
    }

    public int getColumnCount() { return( ItColumn.values().length ); }

    public String getColumnName(int column) {
        return( ItColumn.values()[column].nm );
     }

    public Object getValueAt(int rowIndex, int columnIndex) {

        NetworkInfo ni = getNetwork();
        if ( ni == null ) return("null");

        InstanceStatus s = ni.instances.get(rowIndex);
        if ( s == null ) return("null");

        ItColumn col = ItColumn.values()[columnIndex];

        switch(col) {
            case publicDns:
                return( s.getPublicDnsName() );
            case status:
                try {
                    return(ni.getStateDescription());
                }
                catch(Exception xxx) {
                    ExceptionHandler.text(xxx);
                    return("-");
                }
            case nodeType:
                return( s.getTagValue(InstanceTag.nodeType));
            case launchTime:
                long tm = s.instance.getLaunchTime().getTime();
                if ( tm < Long.MAX_VALUE ) return(UtilPPE.toDateTimeString(tm));
                else return("null");
            case instanceType:
                return(s.instance.getInstanceType());
            case ami:
                return(s.instance.getImageId());
            case virualizationType:
                return(s.instance.getVirtualizationType());        
            default:
                return("Undefined column: " +
                       ((col != null)?col.toString():"null"));
        }
    }

    public void stateChanged() {
        fireTableDataChanged();
    }

        // -----------------------------------------

    private NetworkInfo getNetwork() {
        return( NiM.getForID(networkID) );
    }
}
