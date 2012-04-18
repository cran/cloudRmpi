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
package com.norbl.cbp.ppe.gui;

import java.util.*;
import com.norbl.util.*;
import com.norbl.util.gui.*;
import com.norbl.cbp.ppe.*;
import java.awt.event.*;
import java.io.*;

/**
 *
 * @author moi
 */
public class AmiNameEditor implements ActionListener {
    
    private static String OK = "OK";
    private static String CANCEL = "Cancel";
    private static String ADD_ROW = "Add row";
    
    List<AmiName> amiNames;
    List<TableModelRowable> eiRows;
    EditableTableModel etm;
    EditTableFrame f;
    
    public void create() {
        try {
            amiNames = new ArrayList<AmiName>();
            List<String> names = AmiDescription.getSupportedAmiIDs();
            for ( String nm : names ) {
                amiNames.add(new AmiName(nm));
            }
            eiRows = new ArrayList<TableModelRowable>();
            for ( AmiName ami : amiNames ) eiRows.add(ami);
            
            etm = new EditableTableModel(eiRows);
            
            f = new EditTableFrame("PPE Amazon Machine Images (AMIs)", etm, this);
            
            java.awt.EventQueue.invokeLater(new Runnable() {          
                public void run() { f.create(); }
            });
        }
        catch(Exception xx) { GuiUtil.exceptionMessage(xx); }
    }
    
    public void actionPerformed(ActionEvent e) {
        
        String com = e.getActionCommand();
        
        if ( com.equals(OK) ) {            
            List<TableModelRowable> rows = etm.getRows();            
            List<AmiName> amis = new ArrayList<AmiName>();
            for ( TableModelRowable r : rows ) {               
                AmiName ami = (AmiName) r;
                if ( (ami.name != null) &&
                     (ami.name.length() > 0) ) amis.add(ami);                               
            }
            if ( amis.size() > 0 ) saveAmis(amis);            
            f.dispose();
        }
        else if ( com.equals(CANCEL) ) {          
            f.dispose();
        }
        else if ( com.equals(ADD_ROW) ) {            
            etm.addRow(new AmiName(""));          
        }
    }
    
    void saveAmis(List<AmiName> amis) {
        try {
            File backupFile = null;
            File f = AmiDescription.getAmiIDFile();
            if ( f.exists() ) {
                backupFile = FileUtil.getBackupFilename(f);
                if ( !f.renameTo(backupFile) ) {
                    GuiUtil.warning(new String[] {
                        "Unable to rename " + f.getPath() + "; changes were not saved." },
                         "File rename error");
                    return;
                }
            }

            FileUtil.writeConfigFile(f, toLines(amis), " ");

            GuiUtil.info(new String[] {
                "Updated instance types were written to " + f.getPath(),
                (backupFile != null)?("The old version was saved as " + backupFile.getPath())
                                    :"" },
                "Updated Ec2 Instance Types");        
        }
        catch(IOException iox) { GuiUtil.exceptionMessage(iox); }
    }

    List<String[]> toLines(List<AmiName> amis) {
         List<String[]> lines = new ArrayList<String[]>();
         for ( AmiName ami : amis ) {
             lines.add(new String[] { ami.name  } );
         }
         return(lines);
    }    
}
