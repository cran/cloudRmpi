/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.util.http;

/**
 *
 * @author moi
 */
public class NamedVal {
    
    public String name;
    public String val;

    public NamedVal(String name, String val) {
        this.name = name;
        this.val = val;
    }    
    
    public static NamedVal parsePairToNamedVal(String s) {
        try {
            String[] flds = s.trim().split("=");
            if ( flds.length != 2 ) return(null);
            else return(new NamedVal(flds[0],flds[1]));
        }
        catch(Exception xxx) { return(null); }
    }
    
    public String toString() {
        return(name + "=" + val);
    }
}
