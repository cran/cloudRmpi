/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui.networkspec;

import java.awt.event.*;

/**
 *
 * @author Barnet Wagman
 */
public class ActionCommandNetworkSpec {
    NetworkSpecGui.CmdType cmdType;
    String choiceType;
    String label;
    String value;

    public ActionCommandNetworkSpec(NetworkSpecGui.CmdType cmdType, 
                                    Choice choice, 
                                    String label, 
                                    String value) {
        this.cmdType = cmdType;
        if ( choice != null )
            this.choiceType = choice.getChoiceType();
        this.label = label;
        this.value = value;
    }

    public ActionCommandNetworkSpec(ActionEvent ev) {
        this(ev.getActionCommand());
    }
    
    public ActionCommandNetworkSpec(String cmd) {
        String[] a = cmd.split("#");
        cmdType = NetworkSpecGui.CmdType.valueOf(a[0]);
        choiceType = nonEmptyOrNull(a[1]);
        if ( a.length > 2 )
            label = nonEmptyOrNull(a[2]);
        if ( a.length > 3 )
            value = nonEmptyOrNull(a[3]);
        else value = null;
    }

    public String toString() {
        return( cmdType.toString() + "#" + ssn(choiceType) +
                "#" + ssn(label) + "#" + ssn(value) );
    }

    private String ssn(String x) {
        if ( ( x == null) || (x.trim().length() < 0) ) return(" ");
        else return(x);
    }

    private String nonEmptyOrNull(String x) {
        if ( x == null ) return(x);
        else if ( x.trim().length() < 1 ) return(null);
        else return(x);
    }    
}
