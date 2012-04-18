/*
    Copyright 2011 Northbranchlogic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */


package com.norbl.util.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 *
 * @author Barnet Wagman
 */
public class GuiUtil {

    public static void exceptionMessage(Throwable x) {

        JOptionPane.showMessageDialog(
             getIconFrame(),
            "<html><div width=400>" + 
                x.toString() + "<br><br>" +
                stackTraceToString(x.getStackTrace()) +
            "</div></html>",
            "Exception",
             JOptionPane.ERROR_MESSAGE);
    }
    
    public static void exceptionMessageOnly(Throwable x) {
               
        JOptionPane.showMessageDialog(
             getIconFrame(),           
             x.toString() ,
            "Exception",
             JOptionPane.ERROR_MESSAGE);
    }

       /** Converts a <tt>StackTrace[]</tt> to a <tt>String</tt>, one
         *  item per line.
         */
    public static String stackTraceToString(StackTraceElement[] trace) {
        if ( trace == null ) return("NULL stack trace.");
        String s = "";
        for ( int i = 0; i < trace.length; i++ ) {
            if ( trace[i] != null )
                s += trace[i].toString() + "<br>";
            else s += "null trace element at [" + i + "]";
        }
        return(s);
    }

         // --------- Screen centering ------------------------

    public static void centerOnScreen(Container c) {

	Dimension d = c.getPreferredSize();
	int center_x = GuiMetrics.roundMult(GuiMetrics.getScreenWidth(),0.5f);
	int center_y = GuiMetrics.roundMult(GuiMetrics.getScreenHeight(),0.5f);
	int dx = GuiMetrics.roundMult(d.width,0.5f);
	int dy = GuiMetrics.roundMult(d.height,0.5f);
	c.setLocation(center_x - dx, center_y- dy);
    }

    public static void locateAtUpperRight(Container c) {
        Dimension d = c.getPreferredSize();
        c.setLocation(GuiMetrics.getScreenWidth() - d.width,0);
    }

    public static void locateAtCenterRight(Container c) {
        Dimension d = c.getPreferredSize();
        int dy = GuiMetrics.roundMult(d.height,0.5f);
        int center_y = GuiMetrics.roundMult(GuiMetrics.getScreenHeight(),0.5f);
        c.setLocation(GuiMetrics.getScreenWidth() - (2*d.width),center_y- dy);
    }

        // --------------------------------------------------

        /** A <tt>JOptionPane</tt> confirmation dialog.
         */
    public static boolean answerIsYes(String[] mess,String title) {
        int r = JOptionPane.showConfirmDialog(getIconFrame(), mess, title,
				   JOptionPane.YES_NO_OPTION);

	return(r == JOptionPane.YES_OPTION);
    }

        /**
         *
         * @param mess
         * @param title
         * @return JOptionPane.YES_OPTION, JOptionPane.NO_OPTION,
         *         or JOptionPane.CANCEL_OPTION
         */
    public static int answerIsYesNoCancel(String[] mess,String title) {
        return( JOptionPane.showConfirmDialog(getIconFrame(), mess, title,
				   JOptionPane.YES_NO_CANCEL_OPTION)
               );

    }

    public static void warning(String[] mess,String title) {               
        JOptionPane.showMessageDialog(getIconFrame(), mess, title,
				      JOptionPane.WARNING_MESSAGE);      
    }
    
    public static void info(String[] mess,String title) {
        JOptionPane.showMessageDialog(getIconFrame(), mess, title,
				      JOptionPane.INFORMATION_MESSAGE);
    } 
    
        // -----------------------------------------------------
    
    public static File getFile(String title,File initialDir) {        
        JFileChooser fChooser = new JFileChooser(initialDir);     
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setDialogTitle(title);
        if ( fChooser.showOpenDialog(null) != 
            JFileChooser.APPROVE_OPTION ) return(null);
        File c = fChooser.getSelectedFile();        
        return(c);
    }   
    
        // --------------------------------------------------------
    
    private static JFrame iFrame;
    
    public static JFrame getIconFrame() {
        if ( iFrame == null ) {
            iFrame = new JFrame();
            SwingDefaults.setIcon(iFrame);
        }
        return(iFrame);
    }
}
