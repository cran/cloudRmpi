/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.cbp.ppe.usermonitor;

import com.amazonaws.services.simpledb.model.*;
import com.norbl.util.aws.*;
import com.norbl.util.*;
import java.util.*;

/** Manages the SimpleDb db of users (who have authorized postpay).
 *  <p>
 *  Fields:
 *  <ul>
 *  <li>uid - an email address. This value is also the item name,</li>
 *  <li>eid - the encrypted version of the uid.</li>
 *  <li>callerReference - create for the authorization url</li>
 *  <li>callerReferenceSender</li>
 *  <li>callerReferenceSettlement</li>
 *  <li>tokenID - created by AWS</li>
 *  <li>status - pending|authorized</li>
 *  <li>status_time - time (in long UTC) when status was last set or changed</li> 
 *  <li>n_instance_hours_unbilled - int</li>
 *  <li>n_instance_hours_billed - int</li>
 *  </ul>
 *  
 *  Rows ('items') in the db are always accessed via attribute 'uid'.
 * 
 * @author Barnet Wagman
 */
public class UserDb {
      
    SDBAccess sdbAccess;
    
    public static final String SDB_DOMAIN = "ppe_user_db";
    public static final String NIL = "nil";   
    
    public enum El {
        uid, 
        peid, 
        callerReference, 
        callerReferenceSender,
        callerReferenceSettlement,        
        status, status_time,
        n_instance_hours_unbilled, 
        n_instance_hours_billed,
        settlementTokenID,
        creditSenderTokenID,
        creditInstrumentID
    }
    
    public enum Status { pending, authorized, nil }
    
    public UserDb(SDBAccess sdbAccess) {
        this.sdbAccess = sdbAccess;      
    }
    
    public synchronized boolean userExists(String uid) throws SDBAccessException {                
        return( sdbAccess.getUVal(SDB_DOMAIN,
                                  El.uid.toString(), 
                                  uid, El.uid.toString()) != null );
    }
    
    /** Creates a user's record with status = pending, tokenID=nil, and
     *  all billing values zero.   
     * 
     * @param uid item name and attribute
     * @param peid
     * @param callerReference
     * @param callerReferenceSender
     * @param callerReferenceSettlement 
     */
    public void addUser(String uid,
                        String peid,
                        String callerReference,
                        String callerReferenceSender,
                        String callerReferenceSettlement) {
        
        List<SDBAccess.NamedAttribute> nats =
                new ArrayList<SDBAccess.NamedAttribute>();
        
        nats.add(new SDBAccess.NamedAttribute(El.uid.toString(),
                                              uid));
        nats.add(new SDBAccess.NamedAttribute(El.peid.toString(),
                                              peid));
        nats.add(new SDBAccess.NamedAttribute(El.callerReference.toString(),
                                              callerReference));
        nats.add(new SDBAccess.NamedAttribute(El.callerReferenceSender.toString(),
                                              callerReferenceSender));
        nats.add(new SDBAccess.NamedAttribute(El.callerReferenceSettlement.toString(),
                                              callerReferenceSettlement));        
        nats.add(new SDBAccess.NamedAttribute(El.status.toString(),
                                              Status.pending.toString()));
        nats.add(new SDBAccess.NamedAttribute(El.status_time.toString(),
                               Long.toString(System.currentTimeMillis())));
        nats.add(new SDBAccess.NamedAttribute(El.n_instance_hours_unbilled.toString(),
                                              Integer.toString(0)));
        nats.add(new SDBAccess.NamedAttribute(El.n_instance_hours_billed.toString(),
                                             Integer.toString(0)));
        
        nats.add(new SDBAccess.NamedAttribute(El.settlementTokenID.toString(),
                                              NIL));
        nats.add(new SDBAccess.NamedAttribute(El.creditSenderTokenID.toString(),
                                              NIL));
        nats.add(new SDBAccess.NamedAttribute(El.creditInstrumentID.toString(),
                                              NIL));

        sdbAccess.addAttributes(SDB_DOMAIN,uid,nats);               
    }
    
    /**
     * 
     * @param uid item name
     * @param settlementTokenID
     * @param creditSenderTokenID
     * @param creditInstrumentID 
     */
    public void markAuthorized(String uid,
                               String settlementTokenID,
                               String creditSenderTokenID,
                               String creditInstrumentID) {
       
        List<SDBAccess.NamedAttribute> nats =
                new ArrayList<SDBAccess.NamedAttribute>();
        
        nats.add(new SDBAccess.NamedAttribute(El.settlementTokenID.toString(),
                                              settlementTokenID));
        nats.add(new SDBAccess.NamedAttribute(El.creditSenderTokenID.toString(),
                                              creditSenderTokenID));
        nats.add(new SDBAccess.NamedAttribute(El.creditInstrumentID.toString(),
                                              creditInstrumentID));
        nats.add(new SDBAccess.NamedAttribute(El.status.toString(),
                                          Status.authorized.toString()));
        nats.add(new SDBAccess.NamedAttribute(El.status_time.toString(),
                             Long.toString(System.currentTimeMillis())));
        
        sdbAccess.addAttributes(SDB_DOMAIN,uid,nats);                          
    }
    
    public double calcUnbilledValue(String uid) throws SDBAccessException {        
        int nHours = getIntVal(uid,El.n_instance_hours_unbilled);
        return( (((double) nHours) * ConstantsUM.PRICE_PIPH_DOUBLE) );
    }
    
    /**
     * 
     * @param peid the pseudo encrypted uid.
     */
    public void incrementNInstanceHours(String uid) 
        throws SDBAccessException, NumberFormatException {
        
        int n = getIntVal(uid, El.n_instance_hours_unbilled);
        ++n;
        putIntVal(uid, El.n_instance_hours_unbilled, n);        
    }
    
    /** Mark unbilled values as billed and shift the unbilled values to
     *  billed.
     */
    public void markBilled(String uid) throws SDBAccessException {
               
        int nHoursUnbilled = getIntVal(uid,El.n_instance_hours_unbilled);
               
        int nHoursbilled = getIntVal(uid,El.n_instance_hours_billed);
            
        putIntVal(uid, El.n_instance_hours_unbilled,0);
               
        putIntVal(uid, El.n_instance_hours_billed,nHoursbilled + nHoursUnbilled);        
    }        
    
    
        // -------------------------------------------------------
    
    
    private void putVal(String uid, El elToSet, String val) {        
        sdbAccess.addAttribute(SDB_DOMAIN,uid,
                               elToSet.toString(),val);                 
    }
    
    private String getVal(String uid, String soughtAttributeName) 
        throws SDBAccessException {
        return( sdbAccess.getUVal(SDB_DOMAIN,
                                  El.uid.toString(),
                                  uid,
                                  soughtAttributeName) );
    }
    
    private int getIntVal(String uid,El el) throws SDBAccessException {
        String s = getVal(uid,el.toString());
        if ( s != null ) return(Integer.parseInt(s));
        else throw new SDBAccessException("No value for " + 
                StringUtil.toStringNull(el) + " uid=" + uid);
    }
    
    private void putIntVal(String uid,El el,int val) {
        putVal(uid,el,Integer.toString(val));                 
    }
}
