/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.*;
import javax.crypto.spec.*;

/**
 *
 * @author Barnet Wagman
 */
public class S3Key {
    
    private static final String KEY_BASE = "sem23ozwimo4wsg8";

    /** A peid key is an pseudo encrypted string, modified to be an acceptable 
     *  s3 key.
     *  Because the encryption key is hardcoded into this class, this method
     *  always produces the the same string fro a given key.  However, 
     *  because it is not be decodable.  In general it is not
     *  secure and we don't care.  This is really just a little obfuscation.
     * 
     * @param key
     * @return 
     */
    public static String createPeidKey(String key) throws Exception {
        
        String en = encrypt(key);
        char[] cs = new char [en.length()];
        int j = 0;
        for ( int i = 0; i < cs.length; i++ ) {
            char c = en.charAt(i);
            if ( Character.isDigit(c) || Character.isLetter(c) ) {
                cs[i] = c;
            }
            else cs[i] = Character.forDigit(j, 10);
            if ( ++j > 9 ) j = 0;
        }
        return( new String(cs) );
    }
    
    private static String encrypt(String s) throws Exception {
        return(StringUtil.encrypt(new SecretKeySpec(KEY_BASE.getBytes(),"AES"),s));
    }
}
