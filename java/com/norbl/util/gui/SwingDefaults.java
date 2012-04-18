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

package com.norbl.util.gui;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import com.norbl.util.*;
import javax.swing.text.html.*;
//import nbl.utilj.*;

/**
 *
 * @author Barnet Wagman
 */
public class SwingDefaults {

    public static Font DEFAULT_FONT = new Font(Font.SANS_SERIF,Font.PLAIN,12);
    public static Font DEFAULT_BOLD_FONT = new Font(Font.SANS_SERIF,Font.BOLD, 12);
    public static Font MENU_FONT = new Font(Font.SANS_SERIF,Font.PLAIN,12);

    
    public static void setDefaults() throws Exception {
        UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());

        UIDefaults uids = UIManager.getDefaults();

//        uids.put("TextArea.background",Color.WHITE);
        uids.put("Table.background",Color.white);
//        uids.put("TabbedPane.unselectedBackground",Color.white);
//        uids.put("TabbedPane.tabAreaBackground",Color.white);
//        uids.put("Desktop.background",Color.white);
        uids.put("ScrollPane.background", Color.white);
        uids.put("Panel.background", Color.white);
//        uids.put("List.dropCellBackground",Color.white);
//        uids.put("Table.dropCellBackground",Color.white);
//        uids.put("window",Color.white);
        uids.put("Viewport.background",Color.white);
        uids.put("RadioButton.background", Color.white);
//        uids.put("Label.font",Font.SANS_SERIF);
        uids.put("MenuItem.font",MENU_FONT);
        uids.put("Menu.font",MENU_FONT);
//        uids.put("MenuBar.font",MENU_BAR_FONT);
//        uids.put("PopupMenu.font",MENU_FONT);
        uids.put("OptionPane.questionDialog.titlePane.background",Color.white);
        uids.put("OptionPane.warningDialog.titlePane.background",Color.white);
        uids.put("OptionPane.background",Color.white);
        uids.put("OptionPane.questionDialog.border.background",Color.white);
        uids.put("OptionPane.questionDialog.titlePane.background",Color.white);
        uids.put("InternalFrame.optionDialogBorder",Color.white);
        uids.put("OptionPane.background",Color.white);
        uids.put("OptionPane.questionDialog.border.background",Color.white);
        uids.put("OptionPane.font",DEFAULT_FONT);
        uids.put("Label.font", DEFAULT_FONT);
    }

    public static void getDefaults(String keyPattern) {
        getDefaults(UIManager.getDefaults(),keyPattern);
        getDefaults(UIManager.getLookAndFeelDefaults(),keyPattern);
    }

    public static void getDefaults(UIDefaults defs, String keyPattern) {

//        UIDefaults defs = // UIManager.getLookAndFeelDefaults(); //
//                          UIManager.getDefaults();

        for ( Enumeration e = defs.keys(); e.hasMoreElements(); ) {
            Object x = e.nextElement();
            if ( x instanceof String ) {
                String k = (String) x;
                if ( ((keyPattern != null) &&
                      k.toLowerCase().contains(keyPattern))
                     || (keyPattern == null) ) {
                        System.out.println(k + "  " + defs.get(k));
                }
            }
            else System.out.println("? " + x + "  " + defs.get(x));
        }

    }
    
    public static void setIcon(JFrame f) {
        try {
            ImageIcon icon = 
                new ImageIcon(f.getClass().getResource(
                            "/com/norbl/util/gui/nbl_logo_icon_size_15_b.jpg"));
            f.setIconImage(icon.getImage());       
        }
        catch(Exception xx) { System.err.println(StringUtil.toString(xx)); }
    }
    
    public static ImageIcon getNblIcon() {
        try {
            return(new ImageIcon(SwingDefaults.class.getResource(
                            "/com/norbl/util/gui/nbl_logo_icon_size_15_b.jpg")));
        }
        catch(Exception xx) { 
            System.err.println(StringUtil.toString(xx)); 
            return(null);
        }
    }
       
    static void testAll() throws Exception {
        
//        UIDefaults uids = UIManager.getDefaults();
//        NEXT: for ( Enumeration e = uids.keys(); e.hasMoreElements(); ) {
//            try {
//                Object x = e.nextElement();
//                if ( !(x instanceof String) )  continue NEXT;
//                String k = (String) x;
//                Object z = uids.get(k);
//                if ( z != null ) {
//                    String v = z.toString();
//                    if ( v.toLowerCase().contains("color") ) {
//                        uids.put(k,Color.red);
//                        PPEManager m = new PPEManager(new String [0]);
//                        m.launchGui();
//                        m.frame.setTitle(k);
//                        try { Thread.sleep(1000L); }
//                        catch(InterruptedException ix) {}
//                        m.frame.setVisible(false);// dispose();
//                    }
//                }
//            }
//            catch(Throwable xxx) { System.out.println(xxx.getMessage()); }
//        }
    }

        // -------------------------------------------------

    public static void main(String[] argv) throws Exception {
        getDefaults("label");
//        testAll();
    }
}
