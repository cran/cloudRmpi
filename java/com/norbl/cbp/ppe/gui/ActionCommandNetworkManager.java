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

import java.awt.event.*;

/** Creates and parses action command strings per
 *  <tt>java.awt.event.ActionEvent</tt>.
 *
 *  The action command contains a command name, a network ID (or "nil").
 *
 * @author Barnet Wagman
 */
public class ActionCommandNetworkManager {

    public static final String NIL = "nil";
    public static String DELIM = "#";

    public String cmd;
    public String networkID;   
    
    public ActionCommandNetworkManager(String cmd, String networkID) {
        this.cmd = cmd;
        if ( networkID != null ) this.networkID = networkID;
        else this.networkID = NIL;          
    }

    public ActionCommandNetworkManager(ActionEvent ev) {
        String s = ev.getActionCommand();
        String[] a = s.split(DELIM);
        cmd = a[0].trim();        
        networkID = a[1].trim();       
    }

    public boolean hasNetworkID() {
        return( !networkID.equals(NIL) );
    }
    
    public String toActionEventString() {
        return(cmd + DELIM + networkID);
    }
}
