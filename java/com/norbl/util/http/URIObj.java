/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.util.http;

import java.io.*;
import java.util.*;
import java.net.*;

/** URI as list of {@link NamedVal}.  This class does the
 *  coding, decoding and parsing that's missing from (or buried in ) 
 *  Apache HttpCore. Endoding is per AWS.
 *
 * @author moi
 */
public class URIObj {
    
    public static final String UTF_8_Encoding = "UTF-8";
    
    public String uiDecoded;
    public String localPath;
    public String paramsString;
    public String[] localPathEls;
    
    public List<NamedVal> vals;
    
    public URIObj() {
        vals = new ArrayList<NamedVal>();
    }
    
    public URIObj(String ui) {
        this();
        try {
            uiDecoded = URLDecoder.decode(ui,UTF_8_Encoding);            
            
            if ( !separateLocalPath(uiDecoded) ) return;
                                    
            String[] flds = paramsString.split("&");
            
            for ( String fld : flds ) {
                String[] pair = fld.split("=");
                if ( (pair != null) && (pair.length == 2) ) {
                    vals.add(new NamedVal(pair[0], pair[1]));
                }        
            }    
        }
        catch(UnsupportedEncodingException ex) { 
            throw new RuntimeException(ex); 
        }
    }
    
    public boolean hasLocalPath() { return(localPath != null); }
    
    public String getParam(String paramName) {
        for ( NamedVal nv : vals ) {
            if ( nv.name.equals(paramName) ) return(nv.val);
        }
        return(null);
    }
    
    String toEncodedString() {
        try {
            StringBuilder s = new StringBuilder();
            for ( int i = 0; i < vals.size(); i++ ) {
                NamedVal vn = vals.get(i);
                s.append(vn.name + "=" + vn.val);
                if ( i < (vals.size()-1) ) s.append("&");            
            }
            return( URLEncoder.encode(s.toString(), UTF_8_Encoding) );
        }
        catch(UnsupportedEncodingException ex) { 
            throw new RuntimeException(ex); 
        }
    }    
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        for ( NamedVal nv : vals ) {
            s.append(nv.toString());
            s.append("\n");
        }
        return(s.toString());
    }        
    
    private boolean separateLocalPath(String s) {
        
        int idx = s.indexOf('?');
        if ( idx < 0 ) return(false);
        
        localPath = s.substring(0,idx);
        paramsString = s.substring(idx+1);
        
        localPathEls = stripDangling(localPath, '/').split("/");
        
        return(true);
    }
    
    private String stripDangling(String s,char delim) {
        
        String x = s.trim();
        
        if ( x.charAt(0) == delim ) x = x.substring(1);
        if ( x.charAt(x.length() - 1) == delim)
            x = x.substring(0,x.length()-2+1);
        
            // Keep stripping until nothing changes.
        if ( x.length() < s.length() ) return(stripDangling(x, delim));
        else return(x);        
    } 
}
