/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui;

import com.norbl.cbp.ppe.*;
import com.norbl.util.gui.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
public class MessageDialog {

    
    public static void showNotGoodToGoWarning(JFrame parentFrame,
                                              PPEManager ppeManager) {
        
        JLabel l = new JLabel(
        "<html>" +
        "<br><br>" +
        "<b>The parameters required for accessing AWS need to be entered." +
            "</b><br><br><br>" +
         "You will not be able to launch ec2 instances without them.<br><br>" +
         "To specify these parameters, click 'Enter parameters' below or<br>" +
         "use Edit -> Ec2 parameters (in the menu bar).<br><br><br>" +
         "</html>");
      
        String title = "Ec2 parameters not specified";
        
        Object[] options = new Object[] {
            "Enter parameters",
            "Cancel"
        };
        
        JFrame f;
        if ( parentFrame != null ) f = parentFrame;
        else {
            f = new JFrame();
            SwingDefaults.setIcon(f);
        }
        
        int i = JOptionPane.showOptionDialog(f,
                                             l,
                                             title,
                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,   
                                             options,
                                             0);   
        if ( i == 0 ) {
            ppeManager.editEc2Parameters();
        }           
    }
}
