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

import com.norbl.cbp.ppe.*;

/**
 *
 * @author Barnet Wagman
 */
public class SecurityGroupChoice extends Choice {

    enum Type { defaultGroup, other };

    Type type;

    String label;
    String value;

    public SecurityGroupChoice(Type type) {
        super();
        this.type = type;
        if ( type.equals(Type.defaultGroup) ) {
            label = "Default";
            value = null;
        }
        else if(type.equals(Type.other)) {
            label = "Other: ";
        }
        else throw new RuntimeException("Bad type");        
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() {
        if ( type.equals(Type.defaultGroup) ) return(ConstantsEc2.DEFAULT);
        else if ( type.equals(Type.other) ) {
            String s = ((OtherCC) cc).textField.getText();            
            if ( s.trim().length() > 0 ) return(s);
            else return(null);
        }
        else throw new RuntimeException("Bad type=" + type);
    }

    public String getLabel() { return(label); }
        
    public void createCC() {
        if ( type.equals(Type.defaultGroup) )
            cc = new RadioButtonCC(getLabel());
        else if ( type.equals(Type.other) ) {
            cc = new OtherCC();
            ((OtherCC) cc).setOtherLabel();
            ((OtherCC) cc).createComponents();
            ((OtherCC) cc).populate();                
        }
        cc.setEnabled(enabled);
        cc.setSelected(selected);
    }
}
