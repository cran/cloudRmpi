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

import java.util.*;
import javax.swing.table.*;

/** Table model for a table used for input and editing. 
 *
 * @author moi 
 */
public class EditableTableModel extends AbstractTableModel {
    
    List<TableModelRowable> rows;
  
    /**
     * 
     * @param rows Must have at least one row (a valueless template will do).
     */
    public EditableTableModel(List<TableModelRowable> rows) {
        if ( rows.size() < 1 ) 
            throw new RuntimeException("EditableTableModel() was passed an empty row list.");
        this.rows = rows;
    }        
    
    public int getRowCount() {
        return( rows.size() );
    }
    
    public int getColumnCount() {
       return( rows.get(0).getNColumns() );
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return( rows.get(rowIndex).getColumnValue(columnIndex) );
    }

    public String getColumnName(int column) {
        return( rows.get(0).getColumnName(column) );
    }
   
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return(true);
    }
    
    public void setValueAt(Object value, int row, int col) {        
        TableModelRowable r = rows.get(row);
        r.setColumnValue(col, value);            
        fireTableCellUpdated(row, col); 
    }
    
    
        // ---------------------------------------------------------------
    
    public void addRow(TableModelRowable r) {
        rows.add(r);
        fireTableDataChanged();
    }
    
    public List<TableModelRowable> getRows() { return(rows); }
}
