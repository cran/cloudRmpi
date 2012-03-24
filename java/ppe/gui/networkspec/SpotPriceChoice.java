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
public class SpotPriceChoice extends Choice {

    static String LABEL = "Use spot instances, max price:";

    // "Use spot instances, max price:"

    public SpotPriceChoice() {
        super();       
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    /** This version of {@link #createCC} calls
     *  {@link SpotPriceCC#createComponents()} but NOT
     *  {@link SpotPriceCC#populate() }.
     * 
     */
    public void createCC() {
        cc = new SpotPriceCC(LABEL);
        ((SpotPriceCC) cc).createComponents();
        
        cc.setEnabled(true);
        cc.setSelected(false);
    }
    
    public void populateCC() {
        ((SpotPriceCC) cc).populate();
    }
    
    public void depopulateCC(javax.swing.JFrame frame) {
         ((SpotPriceCC) cc).depopulate(frame);
    }
    
    public void repopulateCC(javax.swing.JFrame frame) {
        ((SpotPriceCC) cc).repopulate(frame);
    }

    public Object getValue() {
        return( ((SpotPriceCC) cc).textField.getText() );
    }

    public double getPrice() {
        try {
            return( Double.parseDouble((String) getValue()) );
        }
        catch(NumberFormatException nfx) { return(Double.NaN); }
    }
    
    public String getLabel() { return(LABEL); }

    public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if ( enabled == false ) {
             ((SpotPriceCC) cc).textField.setText("");
         }
    }

        /** Special handling is required; overrides the parent version.
         *
         * @param selected
         */
     public void setSelected(boolean selected) {

         boolean buttonSelected = ((SpotPriceCC) cc).button.isSelected();

         if ( cc == null ) {
             this.selected = false;
         }
         else { // The radio button is used as a toggl
             if ( buttonSelected ) {
                this.selected = true;
                cc.setSelected(true);
                ((SpotPriceCC) cc).textField.setEnabled(true);
             }
             else {
                this.selected = false;
                cc.setSelected(false);
                ((SpotPriceCC) cc).textField.setEnabled(false);
             }
         }

//         else {
//             String val = (String) getValue();
//             if ( (val == null) || (val.trim().length() < 1) ) {
//                 this.selected = false;
//                 cc.setSelected(false);
//             }
//             else {
//                 this.selected = true;
//                 cc.setSelected(true);
//             }
//         }
    }
}
