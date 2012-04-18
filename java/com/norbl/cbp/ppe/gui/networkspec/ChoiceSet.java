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

import java.util.*;
import javax.swing.*;

/** A set of choices for a variable.  The network spec mostly
 *  consists of these sets.
 *
 * @author Barnet Wagman
 */
public class ChoiceSet extends ArrayList<Choice> {

    public ChoiceSet() {
        super();      
    }

    public boolean hasChoiceType(String choiceType) {
        if ( size() < 1 ) return(false);
        else return(choiceType.equals(get(0).getClass().getSimpleName()));
    }    

    public Choice getChoice(String label) {

        for ( Choice c : this ) {
            if ( c.getLabel().equals(label) ) return(c);
        }
        throw new RuntimeException("No choice matches label=" + label);
    }

    public Choice getChoiceOrNull(String label) {

        for ( Choice c : this ) {
            if ( c.getLabel().equals(label) ) return(c);
        }
        return(null);
    }

    /** Gets the selected choice.  May be null.
     * 
     * @return the selected choice or null if nothing has been selected yet.
     */
    public Choice getSelected() {

        for ( Choice c : this ) {
            if ( c.selected ) return(c);
        }
        return(null);       
    }
    
    public int getNColumns() {
        
        int n0 = this.get(0).getNColumns();
        
        for ( int i = 1; i < this.size(); i++ ) {
            if ( this.get(i).getNColumns() != n0 ) {
                throw new RuntimeException("ChoiseSet n column mismatch for " +
                        this.get(0).getClass().getName());
            }
        }
        return(n0);
    }
}
