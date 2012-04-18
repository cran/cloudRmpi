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


/** Radio button + 'choice' textfield; an 'other' choice.
 *
 * @author Barnet Wagman
 */
public class SpotPriceCC extends OtherCC implements ChoiceComponent {

    public static String OTHER_LABEL = "Other: ";

        /** Used to created an 'other' type cc with a different label.
         *  There really should be another class for this with a more generic name.
         * 
         * @param label
         */
    public SpotPriceCC(String label) {  // ,boolean populateIt) {
        super(); 
        setLabel(label);        
    }

    public SpotPriceCC() { //boolean populateIt) {
        this(OTHER_LABEL); // ,populateIt);
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        textField.setEnabled(false);
        // ^ Text field is disabled.  It will be enabled
        //   in SpotPriceChoice when the radio button is
        //   toggled
    }
}
