/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui.networkspec;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import java.util.*;
import javax.swing.*;

/** In v 1.2, we are using back to a single ('unified') 
 *  network spec gui.  This version if AmiChoice supports ppe-ompi and
 *  cloudRmpi.
 *
 * @author Barnet Wagman
 */
public class AmiChoiceU extends AmiChoice {           

    public AmiChoiceU(AmiPPE ami) { super(ami); }
    
    /** Creates 'other'
     */
    public AmiChoiceU() { super(); }
    
    public int getNColumns() { return(3); } 
    
    public Object getValue(int iCol) {
         
         if ( ami == null ) return(" ");
                  
         switch(iCol) {
            case 0: return(ami.amiID);
            case 1:
                String name = ami.getTagVal(AmiPPE.ATag.nameShort);                
                if ( name == null )
                    name = ami.getTagVal(AmiPPE.ATag.name);
                return(name);                     
            case 2: return(getPiph());    
            default: return("");               
         }
     }
    
     public String getColumnName(int iCol) {                  
         
         switch(iCol) {
            case 0: return(" ");
            case 1: return(" ");                                            
            case 2: return("Price");
            default: return(" ");
         }
     }
     
     public double getColumnWeight(int iCol) {
        
        switch(iCol) {
            case 0: return(0.10);              
            case 1: return(0.30);            
            case 2: return(0.10);                  
            default: return(0.0);
        }      
    }
     
    public int getGridBagAnchor(int iCol) {
        if ( iCol == 0 ) 
            return(java.awt.GridBagConstraints.BASELINE_LEADING);
        else if ( iCol == 1 ) 
            return(java.awt.GridBagConstraints.BASELINE_LEADING);
        else if (iCol <= 2)
            return(java.awt.GridBagConstraints.BASELINE_TRAILING); 
        else return(0); // 'default'
    } 
    
    public java.awt.Insets getInsets(int iCol) {
         switch(iCol) {
            case 0: return(new java.awt.Insets(0,0,0,12));
            case 1: return(new java.awt.Insets(0,0,0,12)); // 12));                         
            case 2: return(new java.awt.Insets(0,0,0,0));                      
            default: return(new java.awt.Insets(0,0,0,0));
        }      
    }
    
    public void setColumnHeaderToolTip(int iCol,JComponent c) {
        if ( getColumnName(iCol).equals("Price") )
            c.setToolTipText("Price per hour per instance");         
    }
    
    public String getToolTip() {
        if ( ami != null ) {       
            String uat = getUnauthorizedToolTipMessage();
            StringBuilder s = new StringBuilder();
            s.append("<html>");
            if ( uat != null ) s.append(uat + "<br><br>");
            String nm = ami.getTagVal(AmiPPE.ATag.nameShort);
            if ( nm == null ) nm = ami.getTagVal(AmiPPE.ATag.name);
            s.append(nm + "<br><br>");
            if ( ami.descriptionHtml != null ) {
                s.append(ami.descriptionHtml);
            }
            else {
                s.append(ami.getTagVal(AmiPPE.ATag.linux));
            }
            s.append("</html>");
            
            return(s.toString());
        }
        else return(null);
    }
          
    private String getPiph() {
        if ( ami == null ) return(" ");
        else return( "$" + StringUtil.f2(ami.imageCharge));
    }  
}
