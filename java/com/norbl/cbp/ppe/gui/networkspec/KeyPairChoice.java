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


/**
 *
 * @author Barnet Wagman
 */
public class KeyPairChoice extends Choice {

    String label;
    String value;

    public KeyPairChoice(String name) {
        super();
        this.label = name;
        if ( !label.equals(OtherCC.OTHER_LABEL) )
            this.value = name;
        else value = null;
    }

        /** Creates 'other'
         *
         * @param name
         */
    public KeyPairChoice() {
        super();
        this.label = "Other: ";
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() {

        if ( !label.equals(OtherCC.OTHER_LABEL) ) {            
            return(value);
        }
        else {
            String val = ((OtherCC) cc).textField.getText();           
            if ( val.trim().length() > 0 ) {
                value = val;
                return(val);
            }
            else {
                value = null;
                return(null);
            }
        }
    }

    public String getLabel() { return(label); }
        
    public void createCC() {
        if ( value != null ) cc = new RadioButtonCC(label);
        else {
            cc = new OtherCC();     
            ((OtherCC) cc).setOtherLabel();
            ((OtherCC) cc).createComponents();
            ((OtherCC) cc).populate();
        } 
        cc.setEnabled(enabled);
        cc.setSelected(selected);        
    }
}
