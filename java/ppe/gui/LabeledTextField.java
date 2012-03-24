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

package ppe.gui;

import javax.swing.*;
import java.awt.event.*;

/**
 *
 * @author Barnet Wagman
 */
public class LabeledTextField extends JPanel {

    static java.awt.Dimension TEXT_FIELD_SZ =
                            new java.awt.Dimension(100, 24);
    static java.awt.Dimension MAX_TEXT_FIELD_SZ =
                            new java.awt.Dimension(300, 24);

    public static java.awt.Dimension TEXT_FIELD_SIZE =
                                new java.awt.Dimension(100, 24);

    JLabel label;
    JTextField textField;

    public LabeledTextField() {
        super();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        label = new JLabel();
        textField = new JTextField();

        label.setText("XXXXX");
        add(label);

        textField.setMaximumSize(TEXT_FIELD_SIZE);
        textField.setMinimumSize(TEXT_FIELD_SIZE);
        textField.setPreferredSize(TEXT_FIELD_SIZE);
        add(textField);
    }

    public void setText(String txt) {
        label.setText(txt);
    }
}
