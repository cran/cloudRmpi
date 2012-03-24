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

/** Radio button + 'choice' textfield; an 'other' choice.
 *
 * @author Barnet Wagman
 */
public class OtherCC extends JPanel implements ChoiceComponent {

    public static String OTHER_LABEL = "Other: ";

    String label;
    JRadioButton button;
    JTextField textField;
    
    public boolean populated;
   
    public OtherCC() { 
        super(); 
        populated = false;
    }
    
    public void setLabel(String label) {
        this.label = label;        
    }
    
    public void setOtherLabel() {
        setLabel(OTHER_LABEL);
    }
    
    public void createComponents() {
       
        button = new JRadioButton();
        if ( label.toLowerCase().startsWith("other") ) {
            button.setMaximumSize(ChoiceComponent.BUTTON_SIZE);
            button.setMinimumSize(ChoiceComponent.BUTTON_SIZE);
            button.setFont(ChoiceComponent.CHOICE_FONT);
        }
        else {
            button.setFont(ChoiceComponent.CHOICE_FONT);
        }
        button.setText(label);
        textField = new JTextField();
        textField.setText("");
        textField.setMaximumSize(ChoiceComponent.TEXT_FIELD_SIZE);
        textField.setMinimumSize(ChoiceComponent.TEXT_FIELD_SIZE);
        textField.setPreferredSize(ChoiceComponent.TEXT_FIELD_SIZE);
        this.setEnabled(true);
        button.setSelected(false);
    }
    
    public boolean isPopulated() { return(populated); }
     
    public void populate() {
        if ( isPopulated() ) return;
        else {
            setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            this.add(button);
            this.add(textField);                 
            populated = true;
        }
    } 
    
    public void repopulate(JFrame parentFrame) {
        if ( isPopulated() ) return;
        else {
            populate();
            this.validate();
            parentFrame.repaint();
        }
    }
    
    public void depopulate(JFrame parentFrame) {
        if ( !isPopulated() ) return;
        else {
            this.removeAll();
            this.revalidate();
            parentFrame.repaint();  
            populated = false;
        }
    }   
    
    public boolean isSelected() {
        return(button.isSelected());
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    public void setSelected(boolean selected) {
        button.setSelected(selected);
    }

    public JRadioButton getRadioButton() { return(button); }

    public void addActionListener(ActionListener actionListener) {
       
        button.addActionListener(actionListener);        
        textField.addActionListener(actionListener);
    }

    public void setActionCommand(String cmd) {        
        textField.setActionCommand(cmd);
        button.setActionCommand(cmd);
    }

    public JComponent getComponent() { return(this); }
}
