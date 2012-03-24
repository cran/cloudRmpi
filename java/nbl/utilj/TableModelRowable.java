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
package nbl.utilj;

import javax.swing.table.*;

/** An object that can (easily) be used as a row in a table model.
 *
 * @author moi
 */
public interface TableModelRowable {
    
    public int getNColumns();
    public String getColumnName(int colIndex);
    public Object getColumnValue(int colIndex);
    public Class getColumnClass(int colIndex);
    public void setColumnValue(int colIndex, Object value);     
}
