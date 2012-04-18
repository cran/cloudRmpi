/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui.networkspec;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
public class AmiChoiceR extends AmiChoice {           

    public AmiChoiceR(AmiPPE ami) { super(ami); }
    
    /** Creates 'other'
     */
    public AmiChoiceR() { super(); }
    
    public int getNColumns() { return(4); } 
    
    public Object getValue(int iCol) {
         
         if ( ami == null ) return(" ");
                  
         switch(iCol) {
            case 0: return(ami.amiID);
//            case 1: return(ami.getTagVal(AmiPPE.ATag.openMPI));
            case 1: return(ami.getTagVal(AmiPPE.ATag.r));
            case 2: return(ami.getTagVal(AmiPPE.ATag.rreval));            
            case 3: return(getPiph());    
            default: return("");               
         }
     }
    
     public String getColumnName(int iCol) {                  
         
         switch(iCol) {
            case 0: return(" ");
//            case 1: return(AmiPPE.ATag.openMPI.key);
            case 1: return(AmiPPE.ATag.r.key);                                
            case 2: return(AmiPPE.ATag.rreval.key);            
            case 3: return("Price");
            default: return(" ");
         }
     }
     
     public double getColumnWeight(int iCol) {
        
        switch(iCol) {
            case 0: return(0.10);              
//            case 1: return(0.10);              
            case 1: return(0.10);            
            case 2: return(0.10);           
            case 3: return(0.10);
            default: return(0.0);
        }      
    }
     
    public int getGridBagAnchor(int iCol) {
        if ( iCol == 0 ) 
            return(java.awt.GridBagConstraints.BASELINE_LEADING);
        else if (iCol <= 3)
            return(java.awt.GridBagConstraints.BASELINE_TRAILING); 
        else return(0); // 'default'
    } 
    
    public java.awt.Insets getInsets(int iCol) {
         switch(iCol) {
            case 0: return(new java.awt.Insets(0,0,0,12));
//            case 1: return(new java.awt.Insets(0,0,0,12));
            case 1: return(new java.awt.Insets(0,0,0,12));    
            case 2: return(new java.awt.Insets(0,0,0,12));                
            case 3: return(new java.awt.Insets(0,0,0,0));                      
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
            return("<html>" +                    
                    ((uat != null)?(uat + "<br><br>"):"") +                      
                    ami.getTagVal(AmiPPE.ATag.name) + "<br><br>" +
                   "Operating system:<br>" +
                    ami.getTagVal(AmiPPE.ATag.linux) +
                   "</html>");  
        }
        else return(null);
    }
          
    private String getPiph() {
        if ( ami == null ) return(" ");
        else return( "$" + StringUtil.f2(ami.imageCharge));
    }
}
