/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.util;

/** Methods to parse string arguments with the format 
 * <tt>&lt;label&gt;=&lt;value&gt;</tt>. Labels are 
 * case insensitive.
 *
 * @author moi
 */
public class ArgvUtil {
    
    /**
     * 
     * @param argv
     * @param label
     * @return val or null
     */
    public static String getVal(String[] argv, String label) {
        try {    
            String ps = label.toLowerCase() + "=";
            for ( String arg : argv ) {
                if ( arg.toLowerCase().startsWith(ps) ) {
                    String[] a = arg.split("=");
                    if ( (a != null) && (a.length == 2) )
                        return(a[1]);
                }
            }
            return(null);
        }
        catch(ArrayIndexOutOfBoundsException ax) {
            throw new RuntimeException("Exception=" + ax +
                    " label=" + label + " length(argv)=" + argv.length +
                    " argv=" + StringUtil.wordsToString(argv));
        }
    }
    
    public static int getIntVal(String[] argv,String label,int defaultVal) {
        String s = getVal(argv,label);
        if ( s != null ) return(Integer.parseInt(s));
        else return(defaultVal);
    }
    
    public static String getVal(String[] argv,String label,String defaultVal) {
        String s = getVal(argv,label);
        if ( s != null ) return(s);
        else return(defaultVal);
    }
}
