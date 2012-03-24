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

import java.util.List;
import ppe.*;
import ppe.ec2.*;
import javax.swing.table.*;

/** The Swing table model supporting network info tables. All information
 *  in this model comes from {@link NetworkInfo} which maintains static
 *  hts of {@link NetworkInfo} objects.
 *
 * @author Barnet Wagman
 */
public class NetworkTableModel extends AbstractTableModel
    implements StateChangeListener {

    private enum NtColumn {
        networkName("Network name"),
        status("Status"),
        nInstances("N ec2 instances"),
        launchTime("Launch time"),
        securityGroup("Security group"),
        rsaKeyname("RSA key name"),
        virtualizationType("Virtualization");

        String nm;
        NtColumn(String nm) { this.nm = nm; }
    }

    public int getRowCount() {
        return(NiM.getNetworks().size());
    }

    public int getColumnCount() { return( NtColumn.values().length ); }

    public String getColumnName(int column) {
        return( NtColumn.values()[column].nm );
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        List<NetworkInfo> nis = NiM.getNetworks();
        if ( nis.size() < (rowIndex + 1) ) return("-");
        
        NetworkInfo ni = nis.get(rowIndex);
        int nInstances = ni.instances.size();
        InstanceStatus ins0 = null;
        if ( nInstances > 0 ) ins0 = ni.instances.get(0);

        NtColumn col = NtColumn.values()[columnIndex];

        switch(col) {
            case networkName:
                return(ni.getNetworkName());
            case status:
                 return( ni.getStateDescription() );
            case nInstances:
                return( Integer.toString(nInstances) );
            case launchTime:
                long lt = ni.getLaunchTime();
                if ( lt < Long.MAX_VALUE ) return(UtilPPE.toDateTimeString(lt));
                else return("null");
            case securityGroup:
                if (ins0 != null)
                    return( UtilPPE.toCsv(ins0.securityGroupNames) );
                else return(" ");
            case rsaKeyname:
                if (ins0 != null)
                    return(ins0.instance.getKeyName());
                else return(" ");
            case virtualizationType:
                if (ins0 != null)
                    return(ins0.instance.getVirtualizationType());
                else return(" ");
            default:
                return("Undefined column: " +
                       ((col != null)?col.toString():"null"));
        }
    }


    public void stateChanged() {
//        /* D */ System.out.println(">>>>> NetworkTableModel.stateChanged(): ");
//        /* D */ for ( int i = 0; i < NiM.getNetworks().size(); i++ )
//                    System.out.println(i + " " +
//                            NiM.getNetworks().get(i).getStateDescription());
        fireTableDataChanged();        
    }

    public int getNetworkNameColumnIndex() {

        for ( int i = 0; i < NtColumn.values().length; i++ ) {
            if ( NtColumn.values()[i].equals(NtColumn.networkName) ) return(i);
        }
        return(-1);
    }    
}
