/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

/**
 *
 * @author Barnet Wagman
 */
public class ConstantsUM {

    public static final String PPE_INFO_BUCKET_NAME = "norbl-ppe-info";
    public static final String BUCKET_NAME_PEID = "norbl-ppe-peids";
    
    /** Note that this bucket is publicly writeable but not publicly 
     *  readable.     
     */
    public static final String BUCKET_NAME_INBOX = "norbl-inbox";
   
    /** Key to s3 object in the info bucket ("norbl-ppe-info")
     *  that contains the public dns of the host running the user monitor.
     */
    public static final String USER_HOSTNAME_KEY = "user-monitor-hostname";
    
    public static final String INSTANCE_TYPE_LIST_KEY = "instance-types";
    
    public static final int JPORT_DEFAULT = 16020;
    public static final int HPORT_DEFAULT = 80;
    
    public static final String PRICE_PIPH = "$0.03";
    public static final double PRICE_PIPH_DOUBLE = 0.03;
    
    public static final String NIL_USER_DATA = "nil";
}
