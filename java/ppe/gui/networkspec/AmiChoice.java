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

package ppe.gui.networkspec;

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import ppe.ec2.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
public class AmiChoice extends Choice {

    com.amazonaws.services.ec2.model.Image image;
        /** Used for other only.         
         */
    String value;

    AmazonEC2Client ec2client;
    
    List<Tag> tags;
    
    public AmiChoice(Image image,AmazonEC2Client ec2Client) {
        super();
        this.image = image;        
        this.ec2client = ec2Client;
        tags = image.getTags();
    }

        /** Creates 'other'
         *
         */
    public AmiChoice() {
        super();
        this.image = null;
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() {
        if ( image != null ) return( image.getImageId() );
        else if ( cc instanceof OtherCC )
            return( ((OtherCC) cc).textField.getText() );
        else if (value != null) return(value);
        else throw new RuntimeException("No image or value, cc: " + cc);
    }

    public String getLabel() {
        if ( image != null ) return(image.getImageId());
        else if ( (cc instanceof OtherCC) || (value != null) )
            return( "Other: " );
        else throw new RuntimeException("No image, cc: " + cc);
    }
    
    public String getDescription() {
        if ( image != null ) return(image.getDescription());
        else return(null);
    }

    public void createCC() {
        if ( image != null ) {
            cc = new RadioButtonCC(getLabel());                        
        }
        else {
            cc = new OtherCC();
            ((OtherCC) cc).setOtherLabel();
            ((OtherCC) cc).createComponents();    
            ((OtherCC) cc).populate();
        } 

        cc.setEnabled(enabled);
        cc.setSelected(selected);
    }       

    public boolean equals(Object other) {
        if ( !(other instanceof AmiChoice) ) return(false);
        else {
            AmiChoice o = (AmiChoice) other;
            return( getValue().equals(o.getValue()) &&
                    getLabel().equals(o.getLabel()) );
        }
    }

    public boolean usesParavirtual() {
        if ( image == null ) return(false);
        else return(Ec2InstanceType.isPv(image.getVirtualizationType()));
    }

     public boolean usesHvm() {
        if ( image == null ) return(false);
       else return(Ec2InstanceType.isHvm(image.getVirtualizationType()));
    }
     
    public int getNColumns() { return(1); } // 3
     
    public Object getValue(int iCol) {
         
         if ( image == null ) return(" ");
                  
         switch(iCol) {
            case 0: return(image.getImageId());
            case 1: return(listToString(getTableVersions(getSoftware(tags))));
            case 2: return(getProductCode());                                
            default: return("");               
         }
     }
    
     public String getColumnName(int iCol) {                  
         
         switch(iCol) {
            case 0: return(" ");
            case 1: return("Versions");
            case 2: return("Product code");                                
            default: return("");
         }
     }
     
     public double getColumnWeight(int iCol) {
        
        switch(iCol) {
            case 0: return(0.10);              
            case 1: return(0.20);              
            case 2: return(0.10);                                    
            default: return(0.0);
        }      
    }
         
    public int getGridBagAnchor(int iCol) {
        switch(iCol) {
            case 0: return(java.awt.GridBagConstraints.BASELINE_LEADING);              
            case 1: return(java.awt.GridBagConstraints.BASELINE_LEADING);                                      
            case 2: return(java.awt.GridBagConstraints.BASELINE_LEADING);              
            default: return(0);
        }    
    } 
     
    public java.awt.Insets getInsets(int iCol) {
         switch(iCol) {
            case 0: return(new java.awt.Insets(0,0,0,12));
            case 1: return(new java.awt.Insets(0,0,0,12));
            case 2: return(new java.awt.Insets(0,0,0,0));                      
            default: return(new java.awt.Insets(0,0,0,0));
        }      
    }
    
    public JComponent getComponent(int iCol) {      
        JLabel x = new JLabel( toStringNullSpace(getValue(iCol)) );      
        return(x);
    }
    
    private String toStringNullSpace(Object x) {
        if ( x != null ) return(x.toString());
        else return(" ");
    }
    
    private String booToString(boolean b) {
        if ( b ) return("yes");
        else return("no");
    }    
     
    public boolean isDevPay() {
        List<ProductCode> pcs = image.getProductCodes();
        return( (pcs != null) && (pcs.size() > 0) );
    }
    
    private String listToString(List<String> strs) {
        if ( strs.size() < 1 ) return("");
        int i = 0;
        StringBuilder s = new StringBuilder();
        NEXT: for ( String str : strs ) {            
            if ( i > 0 ) s.append(", ");
            s.append(str);
            ++i;
        }
        return(s.toString());
    }
    
    String getProductCode() {
        List<ProductCode> pcs = image.getProductCodes();
        if ( pcs.size() < 1 ) return("");
        else return( pcs.get(0).getProductCodeId() );
    }
    
    public String getToolTip() {
              
        String name = getName(tags);
        List<String> sws = getSoftware(tags);
        
        StringBuilder s = new StringBuilder("<html>\n");
        
        s.append("<u>" + image.getImageId() + "</u><br>");
        if ( name.length() > 0 ) s.append(name + "<br>");
        if ( sws.size() > 0 ) {
            s.append("Software:<br>");
            for ( String sw : sws ) {
                s.append("&nbsp;&nbsp;&nbsp;&nbsp;" + sw + "<br>");
            }           
        }        
        
        s.append("Virtualization type: " + image.getVirtualizationType() +"<br>");
        s.append("Region: " +
            toStringNullSpace(AmiDescription.getRegion(ec2client, 
                                    image.getImageId()).getRegionName()) +
                "<br>");
        
        s.append("Product code: " + getProductCode() + "<br>");
        s.append("DevPay instance: " + booToString(isDevPay()) + "<br>");
        s.append("\n<html>");
        
        return(s.toString());
    }
    
    private String getName(List<Tag> tags) {
        for ( Tag t : tags ) {
            if ( "Name".equals(t.getKey()) ) return(t.getValue());
        }
        return("");
    }
          
    private List<String> getSoftware(List<Tag> tags) {
        List<String> sws = new ArrayList<String>();
        NEXT: for ( Tag t : tags ) {
            if ( t.getKey().equals("Name") ) continue NEXT;
            else sws.add(t.getKey() + " " + t.getValue());
        }
        return(sws);
    }    
    
    private List<String> getTableVersions(List<String> str) {
        List<String> rels = new ArrayList<String>();
        for ( String s : str ) {
            if (s.startsWith("ompi ") || s.startsWith("R ") ) {
                rels.add(s);
            }
        }
        return(rels);
    }
}
