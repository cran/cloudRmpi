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

package com.norbl.cbp.ppe.gui.networkspec;

import java.awt.*;
import com.norbl.cbp.ppe.*;
import javax.swing.*;
import com.norbl.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class InstanceTypeChoice extends Choice {

    Ec2InstanceType instanceType;

    public InstanceTypeChoice(Ec2InstanceType instanceType) {
        super();
        this.instanceType = instanceType;      
    }
        
    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() { return(instanceType.instanceType.toString()); }
    public String getLabel() { return(instanceType.instanceType.toString()); }

    public void createCC() {
        cc = new RadioButtonCC(getLabel());
        cc.setEnabled(enabled);
        cc.setSelected(selected);
    }
    
    public int getNColumns() { return(5); }
    
    public String getColumnName(int iCol) {
        
        switch(iCol) {
            case 0: return(" ");              
            case 1: return("Cores");              
            case 2: return("GB ram");             
            case 3: return("Cluster"); 
            case 4: return("Price");             
            default: return("");
        }        
    }
    
    public Object getValue(int iCol) {
        
        switch(iCol) {
            case 0: return(getValue());              
            case 1: return(Integer.toString(instanceType.nCores));              
            case 2: return(StringUtil.f1(instanceType.ramGB));
            case 3: return(booToString(instanceType.clusterSupport));        
            case 4: return("$" + StringUtil.f2(instanceType.pricePiph));            
            default: return("");
        }
    }
    
    private String booToString(boolean b) {
        if ( b ) return("yes");
        else return("no");
    }
    
    public JComponent getComponent(int iCol) {      
        JLabel x = new JLabel( getValue(iCol).toString() );      
//        x.setHorizontalAlignment(SwingConstants.LEFT);
        return(x);
    }
    
    public double getColumnWeight(int iCol) {
        
        switch(iCol) {
            case 0: return(0.40);              
            case 1: return(0.20);              
            case 2: return(0.30);             
            case 3: return(0.30);                      
            case 4: return(0.30);                                
            default: return(0.0);
        }      
    }
    
    public int getGridBagAnchor(int iCol) {
        switch(iCol) {
            case 0: return(GridBagConstraints.BASELINE_LEADING);              
            case 1: return(GridBagConstraints.BASELINE_TRAILING);              
            case 2: return(GridBagConstraints.BASELINE_TRAILING);                        
            case 3: return(GridBagConstraints.BASELINE_TRAILING);              
            case 4: return(GridBagConstraints.BASELINE_TRAILING);                         
            default: return(0);
        }    
    }
    
    public Insets getInsets(int iCol) {
         switch(iCol) {
            case 0: return(new Insets(0,0,0,8));
            case 1: return(new Insets(0,0,0,12));
            case 2: return(new Insets(0,0,0,12));            
            case 3: return(new Insets(0,0,0,12));       
            case 4: return(new Insets(0,0,0,12));               
            default: return(new Insets(0,0,0,0));
        }      
    }
    
    public String getToolTip() {
        
        StringBuilder s = new StringBuilder("<html>\n");        
        s.append(instanceType.description);
        s.append("\n</html>");
                
        return(s.toString());        
    }
    
    public void setColumnHeaderToolTip(int iCol,JComponent c) {
        if ( iCol == 4 ) c.setToolTipText("Price per hour per instance");
        else if ( iCol == 3 ) 
            c.setToolTipText("Can be used launched in cluster placement groups.");
    }
    
    
    private String toNS(Object x) {
        if ( x != null ) return(x.toString());
        else return(" ");
    }
}
