/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.aws.*;
import java.io.*;

/** Static methods for getting and setting the {@link UserMonitor} host name.
 *  There should always be one (and only one) instance of the <tt>UserMonitor</tt>
 *  running.
 *
 * @author Barnet Wagman
 */
public class MonitorHostname {
            
    /** Gets the public dns of this system, iff it is a ec2 instance.
     * 
     * @return public dns or null if the dns cannot be obtained.
     * @throws IOException 
     */
    public static String getThisHostsPublicName() throws IOException {
        return( EC2Utils.getInstanceMetadata("public-hostname") );
    }
    
    
    /** Sets the public s3 host name object to specified string.
     * 
     * @param s3Access 
     * @param hostname
     */
    public static void setHostNameOnS3(S3Access s3Access, String hostname) {
         
        s3Access.putObject(ConstantsUM.PPE_INFO_BUCKET_NAME, 
                           ConstantsUM.USER_HOSTNAME_KEY, 
                           hostname, 
                           true);          
    }
    
    /** This method creates its own {@link S3Access} object. The object
     *  is publicly readable so no IDs are required.
     * 
     * @return the host name or null if the request fails.
     */
    public static String getHostNameFromS3() {
        
        S3Access s3a = null;
        try {
            s3a = new S3Access();
            return((String)
                s3a.getObject(ConstantsUM.PPE_INFO_BUCKET_NAME,
                              ConstantsUM.USER_HOSTNAME_KEY)
             );        
        }
        finally { if ( s3a != null ) s3a.close(); }
    }
}
