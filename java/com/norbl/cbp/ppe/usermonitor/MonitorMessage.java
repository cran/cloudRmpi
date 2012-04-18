/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.cbp.ppe.usermonitor;

import java.io.UnsupportedEncodingException;
import java.util.*;
import org.apache.http.entity.*;

/** We communicate with the {@link Monitor} by sending it http POSTs.
 *  It does not send anything back.<P>
 * 
 *  This class holds a message.  It has methods to create and send
 *  the message as a post and to parse a POST received by the server.<p>
 * 
 *  A message consists of a type and one or more parameter (name,value) pairs.
 *
 * @author moi
 */
public class MonitorMessage {
    
    public enum MessageType {
        createUser,
        isAuthorizedUser,
        recordBillableUse
    }
    
    MessageType type;
    List<NamedVal> vals;
    
    public MonitorMessage(MessageType type) {
        this.type = type;
        vals = new ArrayList<NamedVal>();
    }
    
    public static MonitorMessage parseURI(String uri) {
        try {
            String[] flds = uri.split(" ");
            MessageType type = MessageType.valueOf(flds[0]);

            MonitorMessage m = new MonitorMessage(type);

            NEXT: for ( int i = 1; i < flds.length; i++ ) {
                String[] vp = flds[i].split("=");
                if ((vp == null) || (vp.length != 2) ) continue NEXT;
                m.addVal(vp[0], vp[1]);
            }

            return(m);
        }
        catch(Exception x) {
            System.err.println(x);
            return(null);
        }
    }
    
    /** If name exists, it's val is replaced.
     * 
     * @param name
     * @param val 
     */
    public void addVal(String name,String val) {
        NamedVal ex = getNamedVal(name);
        if ( ex != null ) ex.val = val;
        else vals.add(new NamedVal(name,val));       
    }
    
    NamedVal getNamedVal(String name) {
        for ( NamedVal nv : vals) {
            if (nv.name.equals(name) ) return(nv);
        }
        return(null);
    }
    
    public String getVal(String name) {
        for ( NamedVal nv : vals) {
            if (nv.name.equals(name) ) return(nv.val);
        }
        return(null);
    }
    
    public StringEntity toStringEntity() throws UnsupportedEncodingException {
        
        StringBuilder s = new StringBuilder(type.toString());
        for ( NamedVal nv : vals ) {
            s.append(" " + nv.name + "=" + nv.val);
        }
        
        return(new StringEntity(s.toString(),"UTF-8"));
    }
    
    public static class NamedVal {
        String name;
        String val;
        
        public NamedVal(String name, String val) {
            this.name = name;
            this.val = val;
        }
    }
}
