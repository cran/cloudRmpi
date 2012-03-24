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

import javax.swing.*;
import java.awt.event.*;
import ppe.gui.SwingDefaults;

/**
 *
 * @author Barnet Wagman
 */
public class LabeledTextFieldCC extends JPanel implements ChoiceComponent {

    static java.awt.Dimension TEXT_FIELD_SZ =
                            new java.awt.Dimension(100, 24);
    static java.awt.Dimension MAX_TEXT_FIELD_SZ =
                            new java.awt.Dimension(300, 24);
    
    static int WIDTH_PREF = 100;
    static int WIDTH_MAX = 300;

    JLabel label;
    JTextField textField;

//    public LabeledTextFieldCC(String labelText) {
//
//        super();
//        
//        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
//        label = new JLabel();
//        textField = new JTextField();
//
//        label.setText(labelText);
//        label.setFont(SwingDefaults.DEFAULT_BOLD_FONT);
//        add(label);
//
//        textField.setMaximumSize(MAX_TEXT_FIELD_SZ);
//        textField.setMinimumSize(TEXT_FIELD_SZ);
//        textField.setPreferredSize(TEXT_FIELD_SZ);
//        add(textField);
//    }

//    public LabeledTextFieldCC() {
//
//        super();
//
//        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
//        label = new JLabel();
//        textField = new JTextField();
//
//        label.setText("XXXXX");
//        add(label);
//
//        textField.setMaximumSize(TEXT_FIELD_SIZE);
//        textField.setMinimumSize(TEXT_FIELD_SIZE);
//        textField.setPreferredSize(TEXT_FIELD_SIZE);
//        add(textField);
//    }
    
    public LabeledTextFieldCC(int minWidth, int maxWidth) {
        super();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        label = new JLabel();
        textField = new JTextField();
        
        label.setText("XXXXX");
        add(label);
        
        setWidths(minWidth,maxWidth);
        add(textField);
    }
    
    public LabeledTextFieldCC(String labelText, int minWidth, int maxWidth) {
        this(minWidth,maxWidth);
        label.setText(labelText);
    }
    
    public LabeledTextFieldCC() {
        this(WIDTH_PREF,WIDTH_MAX);
    }
    
    public LabeledTextFieldCC(String labelText) {
        this(labelText,WIDTH_PREF,WIDTH_MAX);
    }   
    
    public void setWidths(int minWidth, int maxWidth) {
        java.awt.Dimension pref = new java.awt.Dimension(minWidth,24);
        java.awt.Dimension max = new java.awt.Dimension(maxWidth,24);
        textField.setMaximumSize(max);
        textField.setMinimumSize(pref);
        textField.setPreferredSize(pref);         
    }
   
    java.awt.Dimension getTextDim(int width) {
        return(new java.awt.Dimension(width,24));
    }

    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        label.setEnabled(enabled);
    }

    public void setSelected(boolean selected) {}

    public JRadioButton getRadioButton() { return(null); }

    public void addActionListener(ActionListener actionListener) {
        textField.addActionListener(actionListener);
    }

    public void setActionCommand(String cmd) {
        textField.setActionCommand(cmd);
    }

    public JComponent getComponent() {
        return(this);
    }

    public boolean isSelected() {
        return(true);
    }
}
