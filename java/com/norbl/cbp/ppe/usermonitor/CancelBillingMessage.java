/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import java.io.*;

/** <b><i>NOTE</i></b> that this class does NOT extend
 *  {@link Message} and is not sent to {@link UserMonitor}.
 *  When a user wants to cancel billing authorization, this object
 *  is written to the s3 bucket norbl-inbox.
 *
 * @author Barnet Wagman
 */
public class CancelBillingMessage implements Serializable {    
        static final long serialVersionUID = 0L; 
    
    public String uid;
    public String key;
    
    public CancelBillingMessage(String uid) throws Exception {
        this.uid = uid;
        key = S3Key.createPeidKey(uid) + "-" + 
              Long.toString(System.currentTimeMillis());
    }        
}
