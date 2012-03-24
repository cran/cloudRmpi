/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ppe.gui;

import java.awt.event.*;
import java.util.*;
import ppe.ec2.*;
import java.io.*;
import nbl.utilj.*;
import ppe.UtilPPE;

/**
 *
 * @author moi
 */
public class InstanceTypeEditor implements ActionListener {
    
    private static String OK = "OK";
    private static String CANCEL = "Cancel";
    private static String ADD_ROW = "Add row";
    
    List<Ec2InstanceType> initialInstanceTypes;
    List<TableModelRowable> eiRows;
    EditableTableModel etm;
    EditTableFrame f;
    
    public void create() {
        try {
            initialInstanceTypes = Ec2InstanceType.getInstanceTypes();

            eiRows = new ArrayList<TableModelRowable>();
            for ( Ec2InstanceType et : initialInstanceTypes ) eiRows.add(et);

            etm = new EditableTableModel(eiRows);                

            f = new EditTableFrame("Ec2 Instance Types", etm, this);

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
            List<Ec2InstanceType> ets = new ArrayList<Ec2InstanceType>();
            for ( TableModelRowable r : rows ) {
                Ec2InstanceType eit = (Ec2InstanceType) r;
                if ( eit.isFullySpecified() ) ets.add(eit);               
            }
            if ( ets.size() > 0 ) saveInstanceTypes(ets);            
            f.dispose();
        }
        else if ( com.equals(CANCEL) ) {          
            f.dispose();
        }
        else if ( com.equals(ADD_ROW) ) {
            if ( true) throw new UnsupportedOperationException(); /* D */
            /* D */ Ec2InstanceType eit = null;// new Ec2InstanceType("", Ec2InstanceType.VirtualizationType.hvm,1,"");
            etm.addRow(eit);          
        }
    }
        
    void saveInstanceTypes(List<Ec2InstanceType> instanceTypes) {
        try {
            File backupFile = null;
            File f = Ec2InstanceType.getInstanceTypeFile();
            if ( f.exists() ) {
                backupFile = UtilPPE.getBackupFilename(f);
                if ( !f.renameTo(backupFile) ) {
                    GuiUtil.warning(new String[] {
                        "Unable to rename " + f.getPath() + "; changes were not saved." },
                         "File rename error");
                    return;
                }
            }

            UtilPPE.writeConfigFile(f, toLines(instanceTypes), " ");

            GuiUtil.info(new String[] {
                "Updated instance types were written to " + f.getPath(),
                (backupFile != null)?("The old version was saved as " + backupFile.getPath())
                                    :"" },
                "Updated Ec2 Instance Types");        
        }
        catch(IOException iox) { GuiUtil.exceptionMessage(iox); }
    }

    List<String[]> toLines(List<Ec2InstanceType> eits) {
        List<String[]> lines = new ArrayList<String[]>();
        for ( Ec2InstanceType ei : eits ) {
            lines.add(new String[] {
                ei.instanceType.toString(), 
                ei.vt.toString(), 
                Integer.toString(ei.nCores),
                ei.description 
             });            
        }
        return(lines);
    }    
}
