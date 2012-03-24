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

import java.awt.*;
import java.awt.Insets;
import javax.swing.*;

/** One of the possible choices for a variable that is specified via
 *  {@link NetworkSpecFrame}.
 *
 * @author Barnet Wagman
 */
abstract public class Choice {

    ChoiceComponent cc;

    boolean enabled, selected;

    public Choice() {
        enabled = true;
        selected = false;
    }

    /** Creates a choice {@link Component} that can be added to a
     *  {@link javax.swing.JPanel}.  If the component is itself a 
     *  {@link javax.swing.JPanel},
     *  its components are created but not loaded. That's the responsibility of
     *  the {@link #load()} method.
     */
    public abstract void createCC();
    public void load() {}

    public abstract Object getValue();
    public abstract String getLabel();
    public abstract String getChoiceType();
    
    public void setSelected(boolean selected) {
        this.selected = selected;
        if ( cc != null ) cc.setSelected(selected);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if ( cc != null ) cc.setEnabled(enabled);
    }
    
    /** Default implementation: returns 1.
     * 
     * @return 
     */
    public int getNColumns() {
       return(1); 
    }
    
    /** Default implementation: returns {@link #getLabel() }
     * 
     * @return 
     */   
    public String getColumnName(int iCol) {
        return(getLabel());
    }
    
    /** Default implementation: returns {@link #getValue() }
     * 
     * @return 
     */
    public Object getValue(int iCol) {
        return(getValue());
    }
    
    public JComponent getComponent() { return(cc.getComponent()); }
    
    /** Default implementation: returns 
     *  {@link ChoiceComponent#getComponent()  }
     * 
     * @return 
     */
    public JComponent getComponent(int iCol) {
        return(cc.getComponent());
    }
    
    /** Default implementation: returns 1.0
     * 
     * @return 
     */    
    public double getColumnWeight(int iCol) { return(1.0); }
    
    /** Default implementation: 
     *  returns GridBagConstraints.BASELINE_LEADING. 
     * @return 
     */    
    public int getGridBagAnchor(int iCol) {
        return(GridBagConstraints.BASELINE_LEADING);
    }
    
    /** Default implementation: all zeros.
     * 
     * @param iCol
     * @return 
     */
    public Insets getInsets(int iCol) {
        return(new Insets(0,0,0,0));
    }
    
    public String getToolTip() { return(""); }
}
