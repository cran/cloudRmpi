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

import javax.swing.*;
import java.awt.event.*;

/** A <tt>JComponent</tt> used to specify a choice.  Every {@link Choice}
 *  creates an appropriate {@link ChoiceComponent} but a {@link Choice}
 *  can supply other information as well (such as a description).<p>
 *  
 *  A {@link ChoiceComponent} is either a <tt>JPanel</tt> or something
 *  (like a <tt>JRadioButton</tt> that can go into <tt>JPanel</tt>.
 *  Non-JPanel choice componts are usually displayed in a 
 *  {@link ChoiceSet}, which extends <tt>JPanel</tt>.
 *
 * @author Barnet Wagman
 */
public interface ChoiceComponent {

    public void setEnabled(boolean enabled);
    public void setSelected(boolean selected);

    public JRadioButton getRadioButton();

    public void addActionListener(ActionListener actionListener);
    public void setActionCommand(String cmd);

    public JComponent getComponent();

    public boolean isSelected();

    public static java.awt.Dimension BUTTON_SIZE =
                                new java.awt.Dimension(100, 22);
    public static java.awt.Font CHOICE_FONT =
                                new java.awt.Font("Arial", 0, 13);
    public static java.awt.Font CHOICE_FONT_BOLD =
                                new java.awt.Font("Arial",java.awt.Font.BOLD, 13);

    public static java.awt.Dimension TEXT_FIELD_SIZE =
                                new java.awt.Dimension(100, 24);

}
