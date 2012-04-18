/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.aws.*;
import java.io.*;


/** We maintain a user's status in a S3, in publicly readable objects (one
 *  per user). This class manages those objects, which are static 
 *  inner classes of {@link UserStatus}. The objects names are the users
 *  eid.  Note that the {@link UserStatus.Status} objects just contains the eid
 *  and an {@link UserDb} enum <tt>Status</tt>, nothing else.
 *
 * @author Barnet Wagman
 */
public class UserStatus implements Serializable {    
    static final long serialVersionUID = 0L; 

    
    
    public static enum Status { pending, authorized, nil }
    
    S3Access s3Access;
    
    public UserStatus(S3Access s3Access) {
        this.s3Access = s3Access;       
    }
  
    public synchronized void putStatus(String peid,UserStatus.Status s) {
        s3Access.putObject(ConstantsUM.BUCKET_NAME_PEID, peid, s, true);
    }
    
    public synchronized UserStatus.Status getStatus(String peid) {
        return((UserStatus.Status) 
                s3Access.getObject(ConstantsUM.BUCKET_NAME_PEID, peid));
    }
     
    
  
}
