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

package ppe.gui.networkspec;

/**
 *
 * @author Barnet Wagman
 */
public class NInstancesChoice extends Choice {

    static String LABEL = "Number of instances: ";

    public NInstancesChoice() {
        super();
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public void createCC() {
        cc = new LabeledTextFieldCC(LABEL);
        cc.setEnabled(enabled);
        cc.setSelected(selected);
    }

    public Object getValue() {
        return( ((LabeledTextFieldCC) cc).textField.getText() );
    }

    public int getNInstances() {
        try {
            return( Integer.parseInt((String) getValue()) );
        }
        catch(NumberFormatException nfx) { return(-1); }
    }

    public String getLabel() { return(LABEL); }

}
