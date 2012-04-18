/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import java.io.*;
import com.norbl.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class Message implements Serializable {    
    static final long serialVersionUID = 0L;           
    
    public static class AddUser extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
        
        public String uid;
                
        public AddUser(String uid) { this.uid = uid; }
    }
    
    public static class NewUserUrl extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
     
        public String uid;
        public String eid;
        public String url; 
        
        public NewUserUrl(String uid,String eid,String url) {
            this.uid = uid;
            this.eid = eid;
            this.url = url;
        }
        
        public String toString() {
            return("uid=" + uid + "  eid=" + eid + "\n" +
                   "url=\n" + url); 
        }
    }
    
    public static class BillableUsage extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
               
        public String uid;
        
        public BillableUsage(String uid) { 
            this.uid = uid; 
        }
        
        public String toString() { return("Billable usage for " + uid); }
    }
    
    public static class Ok extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
        
        public String toString() { return("Ok"); }
    }
    
    public static class Error extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
        
        public Throwable exception;
        
        public Error(Throwable exception) {
            this.exception = exception;
        }                        
        
        public String toString() {
            return(StringUtil.toString(exception));
        }
    }
    
    public static class TestMessage extends Message implements Serializable {    
        static final long serialVersionUID = 0L; 
               
        public String messsage;
        
        public TestMessage(String message) { 
            this.messsage = message;
        }
        
        public String toString() { 
            return(messsage);
        }
    }
}
