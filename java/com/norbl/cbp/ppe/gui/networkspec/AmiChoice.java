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

import com.norbl.cbp.ppe.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
abstract public class AmiChoice extends Choice {

    AmiPPE ami;
    
        /** Used for other only.         
         */
    String value;

    public AmiChoice(AmiPPE ami) {
        super();
        this.ami = ami;                      
    }

        /** Creates 'other'
         *
         */
    public AmiChoice() {
        super();
        ami = null;
    }
    
    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() {
        if ( ami != null ) return( ami.amiID );
        else if ( cc instanceof OtherCC )
            return( ((OtherCC) cc).textField.getText() );
        else if (value != null) return(value);
        else throw new RuntimeException("No image or value, cc: " + cc);
    }

    public String getLabel() {
        if ( ami != null ) return(ami.amiID);
        else if ( (cc instanceof OtherCC) || (value != null) )
            return( "Other: " );
        else throw new RuntimeException("No image, cc: " + cc);
    }
       
    public void createCC() {
        if ( ami != null ) {
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
        if ( ami == null ) return(false);
        else return(Ec2InstanceType.isPv(ami.getVirtualizationType()));
    }

     public boolean usesHvm() {     
       if ( ami == null ) return(false);
       else {          
           return(Ec2InstanceType.isHvm(ami.getVirtualizationType()));
       }
    }
     
    abstract public int getNColumns();     
    abstract public Object getValue(int iCol);    
    abstract public String getColumnName(int iCol);
    abstract public double getColumnWeight(int iCol);    
    abstract public int getGridBagAnchor(int iCol);     
    abstract public java.awt.Insets getInsets(int iCol);
    
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
    
    protected String getUnauthorizedToolTipMessage() {
        if ( ami.isUsableReBilling(PPEManager.paramsEc2) ) return(null);
        else return("To use this AMI, you must authorize billing.<br>" +
                    "Use 'Account -> Authorize instance billing'");
    }
}
