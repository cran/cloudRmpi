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

package com.norbl.util;

import java.text.*;
import java.util.*;
import javax.crypto.*;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Barnet Wagman
 */
public class StringUtil {

    public static boolean isValidBooleanFlag(String value) {
        String lv = value.toLowerCase();
        return( lv.equals("true") || lv.equals("false") );
    }
    
    public static String f1(double x) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(1);
        f.setMinimumFractionDigits(1);
        return(f.format(x));
    }
    
    public static String f2(double x) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(2);
        f.setMinimumFractionDigits(2);
        return(f.format(x));
    }

    public static String toCsv(List<String> strs) {
        StringBuilder s = new StringBuilder();
        for ( int i = 0; i < strs.size()-1; i++ ) {
            s.append(strs + ", ");
        }
        s.append(strs.get(strs.size()-1));
        return(s.toString());
    }  
    
    public static boolean containsWhiteSpace(String s) {
        for ( int i = 0; i < s.length(); i++ ) {
            if ( Character.isWhitespace(s.charAt(i)) ) return(true);
        }
        return(false);
    }
    
    /** @return the stack trace and causal message as string
     * 
     * @param x
     * @return 
     */
    public static String toString(Throwable x) {
        return(exceptionStackToString(x) + "\n\n" +
               getExceptionMessage(x)
               );
    }
    
    public static String getExceptionMessage(Throwable x) {
        Throwable c = x.getCause();
        if ( (c != null) && (c.getMessage() != null) &&
             (c.getMessage().length() > 0) ) {
            return(c.getMessage());
        }
        else return( x.toString() );  
    }
    
    public static String exceptionStackToString(Throwable x) {
        if ( x == null ) return("NULL exception!");        
        return(throwableToString(x,""));
    }
    
    private static String throwableToString(Throwable x,String prefix) {
        
        String mess = x.toString() + "\n\n" +
                      stackTraceToString(x.getStackTrace());
        Throwable cause = x.getCause();
        if ( cause != null ) {
            mess += "\nCAUSE:\n" + throwableToString(cause,prefix + "    ");
        }
        return(mess);
    }
    
        /** Converts a <tt>StackTrace[]</tt> to a <tt>String</tt>, one
         *  item per line.
         */
    public static String stackTraceToString(StackTraceElement[] trace) {
        if ( trace == null ) return("NULL stack trace.");
        String s = "";
        for ( int i = 0; i < trace.length; i++ ) {
            if ( trace[i] != null )
                s += trace[i].toString() + "\n";
            else s += "null trace element at [" + i + "]";
        }
        return(s);
    }  
    
    public static String wordsToString(String[] words) {
	if ( words.length < 1 ) return("");
	String s = words[0];
	for (int i = 1; i < words.length; i++ ) s += " " + words[i];
	return(s);
    }
    
    public static String toStringNull(Object s) {
        if ( s != null ) return(s.toString());
        else return("null");
    }
    
    public static String encrypt(SecretKey key, String s) throws Exception {        
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] enb = c.doFinal(s.getBytes());        
        Base64 b64enc = new Base64();       
        return(new String(b64enc.encode(enb)));    
    }
    
    public static String decrypt(SecretKey key,String en) throws Exception {
        Base64 b64enc  = new Base64();
        byte[] enb = b64enc .decode(en.getBytes());
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] denb = c.doFinal(enb);       
        return(new String(denb));
    }
}
