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
import java.awt.*;

/**
 *
 * @author Barnet Wagman
 */
public class ChoiceMenu extends JPanel {


    ButtonGroup buttonGroup;

    /** The constructor just calls super() to create the JPanel.
     *  Nothing is loaded here.    
     */
    public ChoiceMenu() { super(); }

    public void build(ChoiceSet cs, ActionListener actionListener,
                      int nRows) {             
        if ( cs.getNColumns() == 1 ) buildSingleColumnMenu(cs,actionListener,nRows);
        else buildMultiColumnMenu(cs,actionListener);
    }
    
    public void emptyMenu() {
        this.removeAll();
    }
     
    private void buildSingleColumnMenu(ChoiceSet cs, 
                                       ActionListener actionListener,
                                       int nRows) {
        removeAll(); // Added 5/16/2011 to support zone choices    
        setLayout(new java.awt.GridLayout(nRows,1));       
        
        buttonGroup = new ButtonGroup();
                
        for ( Choice c : cs ) {         
            c.createCC();           
            c.cc.addActionListener(actionListener);
            Object val = c.getValue();
            c.cc.setActionCommand(
                new NetworkSpecGuiFrame.AC(
                        NetworkSpecGuiFrame.CmdType.choice,
                        c,
                        c.getLabel(),
                        (val != null)?val.toString():"null").toString());
            JRadioButton b = c.cc.getRadioButton();
            b.setToolTipText(c.getToolTip());
            if ( b != null ) buttonGroup.add(b);
            this.add(c.getComponent());
            if ( c.enabled ) c.cc.setEnabled(true); // Added 5/16/2011 supporting zone               
        }
    }     
    
    private void buildMultiColumnMenu(ChoiceSet cs, 
                                      ActionListener actionListener) {
       
        removeAll(); // Added 5/16/2011 to support zone choices    
        setLayout(new java.awt.GridBagLayout());

        buttonGroup = new ButtonGroup();
            
        Choice c0 = cs.get(0);
        
            // Add column headings.
        GridBagConstraints gbc = new GridBagConstraints();
        for ( int iCol = 0; iCol < cs.getNColumns(); iCol++ ) {
            gbc = new GridBagConstraints();
            gbc.gridx = iCol;
            gbc.gridy = 0;
            gbc.anchor = c0.getGridBagAnchor(iCol); // GridBagConstraints.BASELINE_LEADING;
            gbc.weightx = c0.getColumnWeight(iCol);
            gbc.insets = c0.getInsets(iCol);
            JLabel t = new JLabel(c0.getColumnName(iCol));
            Font f = t.getFont();
            t.setFont(new Font(f.getName(),Font.BOLD,f.getSize()));
            this.add(t,gbc);
        }
        
            // Add the rows of data.
        for (int iRow = 0; iRow < cs.size(); iRow++) {
            Choice c = cs.get(iRow);
                   
                 // Create and setup the radio button
            c.createCC();
            c.cc.addActionListener(actionListener);
            Object val = c.getValue();
            c.cc.setActionCommand(
                new NetworkSpecGuiFrame.AC(
                        NetworkSpecGuiFrame.CmdType.choice,
                        c,
                        c.getLabel(),
                        (val != null)?val.toString():"null").toString());
            JRadioButton b = c.cc.getRadioButton();
            b.setToolTipText(c.getToolTip());
            if ( b != null ) buttonGroup.add(b);
            
            if ( c.enabled ) c.cc.setEnabled(true); // Added 5/16/2011 supporting zone
           
                // Add components            
            
                // Add the cc component
            gbc.gridx = 0;
            gbc.gridy = iRow+1;
            gbc.weightx = c.getColumnWeight(0);
            gbc.anchor = c.getGridBagAnchor(0); // GridBagConstraints.BASELINE_LEADING;
            gbc.insets = c0.getInsets(0);
            this.add(c.cc.getComponent(),gbc );
            
            for ( int iCol = 1; iCol < c.getNColumns(); iCol++ ) {
                gbc = new GridBagConstraints();
                gbc.gridx = iCol;
                gbc.gridy = iRow+1;
                gbc.anchor = c.getGridBagAnchor(iCol); // GridBagConstraints.BASELINE_LEADING;
                gbc.weightx = c.getColumnWeight(iCol);
                gbc.insets = c0.getInsets(iCol);
                this.add(c.getComponent(iCol),gbc);
            }
        }
    }
}
