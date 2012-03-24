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
public class SlotsPerHostChoice extends Choice {

    static String LABEL = "Slots per host: ";
    int nSlots;

    public SlotsPerHostChoice(int nSlots) {
        super();
        this.nSlots = nSlots;
    }

    public SlotsPerHostChoice() {
        super();
        this.nSlots = -1;
    }

    public void createCC() {
        cc = new LabeledTextFieldCC(LABEL);
        cc.setEnabled(enabled);
        cc.setSelected(selected);
        if ( nSlots > 0 )
            ((LabeledTextFieldCC) cc).textField.setText(Integer.toString(nSlots));
        else ((LabeledTextFieldCC) cc).textField.setText("");
    }

    public Object getValue() {
        try {
            String s = ((LabeledTextFieldCC) cc).textField.getText();
            nSlots = Integer.parseInt(s);
            if ( nSlots > 0 ) return(new Integer(nSlots));
            else return(null);
        }
        catch(NumberFormatException nfx) {
            return(null);
        }
    }

    public int getNSlots() { return(nSlots); }

    public String getLabel() { return(LABEL); }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public void setNSlots(int nSlots) {
        this.nSlots = nSlots;
        if ( nSlots > 0 )
            ((LabeledTextFieldCC) cc).textField.setText(Integer.toString(nSlots));
        else ((LabeledTextFieldCC) cc).textField.setText("");
    }
}
