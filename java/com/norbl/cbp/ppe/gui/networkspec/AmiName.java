/*
    Copyright 2012 Northbranchlogic, Inc.

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

package com.norbl.cbp.ppe.gui.networkspec;

import com.norbl.util.gui.*;

/** Ami (image).  This class is used only for editing the ami-ID file.
 *
 * @author moi
 */
public class AmiName implements TableModelRowable {
    
    public String name;
    
    public AmiName(String name) {
        this.name = name;
    }
    
    public Class getColumnClass(int colIndex) { return(String.class); }    
    public String getColumnName(int colIndex) { return("AMI Name"); }
    public Object getColumnValue(int colIndex) { return(name); }
    public int getNColumns() { return(1); }
    
    public void setColumnValue(int colIndex, Object value) {
        if ( !(value instanceof String) ) {
            GuiUtil.warning(new String[] {
                "Illegal value class=" +
                ((value != null)?value.getClass().getName():"null")                
                },
                "Illegal value");
            return;
        }
        else name = (String) value;    
    }
}
