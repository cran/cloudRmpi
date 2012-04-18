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

package com.norbl.cbp.ppe.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 *
 * @author Barnet Wagman
 */
public class LabeledTextField extends JPanel {

    static java.awt.Dimension TEXT_FIELD_SZ =
                            new java.awt.Dimension(100, 24);
    static java.awt.Dimension MAX_TEXT_FIELD_SZ =
                            new java.awt.Dimension(300, 24);
    
    static int WIDTH_PREF = 100;
    static int WIDTH_MAX = 300;

    JLabel label;
    JTextField textField;

    public LabeledTextField(int minWidth, int maxWidth) {
        super();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        label = new JLabel();
        textField = new JTextField();
               
        label.setText("XXXXX");        
        setWidthsLabel(minWidth);
        add(label);
        
        setWidths(minWidth,maxWidth);
        add(textField);
        
        setEnabled(true);
    }
    
    public void setLabelTextBold() {
        Font f = label.getFont();
        f = new Font(f.getFontName(),Font.BOLD,f.getSize());
        label.setFont(f);
    }
    
    public LabeledTextField(String labelText, int minWidth, int maxWidth) {
        this(minWidth,maxWidth);
        label.setText(labelText);
    }
    
    public LabeledTextField() {
        this(WIDTH_PREF,WIDTH_MAX);
    }
    
    public LabeledTextField(String labelText) {
        this(labelText,WIDTH_PREF,WIDTH_MAX);
    }   
    
    public void setWidths(int minWidth, int maxWidth) {
        java.awt.Dimension pref = new java.awt.Dimension(minWidth,24);
        java.awt.Dimension max = new java.awt.Dimension(maxWidth,24);
        textField.setMaximumSize(max);
        textField.setMinimumSize(pref);
        textField.setPreferredSize(pref);         
    }
    
    public void setWidthsLabel(int minWidth) {
        int maxWidth = 140;
        java.awt.Dimension pref = new java.awt.Dimension(minWidth,24);
        java.awt.Dimension max = new java.awt.Dimension(maxWidth,24);
        label.setMaximumSize(max);
        label.setMinimumSize(pref);
        label.setPreferredSize(pref);         
    }
   
    java.awt.Dimension getTextDim(int width) {
        return(new java.awt.Dimension(width,24));
    }

    public void setVal(String val) {
        textField.setText(val);
    }
    
    public String getVal() {
        return(textField.getText());
    }
    
    public String getKey() {
        return(label.getText());
    }
    
    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        label.setEnabled(enabled);
    }
    
    public void addActionListener(ActionListener actionListener) {
        textField.addActionListener(actionListener);
    }   
}
