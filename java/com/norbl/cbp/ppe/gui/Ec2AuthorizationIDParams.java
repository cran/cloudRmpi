/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import java.io.*;

/** Holds the params that are required as identification for launching
 *  instances. This class is used only when entering/editing the params.<p>
 * 
 *  They are normally stored in config file and accessed via 
 *  {@link ParamsEc2}
 *
 * @author Barnet Wagman
 */
public class Ec2AuthorizationIDParams {

    public String accountNumber;
    public String accessKeyID;
    public String secretAccessKey;
    public String rsaKeyName;
    public File rsaKeyPairFile;
    
    public boolean paramsOk() {
        return( (accountNumber != null) &&
                (accessKeyID != null) &&
                (secretAccessKey != null) &&
                (rsaKeyName != null) &&
                (rsaKeyPairFile != null) &&
                rsaKeyPairFile.exists()
              );
    }
    
    public void putInto(ParamHt paramHt) {
        
        paramHt.put(ParamsEc2.ParamName.awsUserID.toString(), accountNumber);
        paramHt.put(ParamsEc2.ParamName.awsAccessKey.toString(),accessKeyID);
        paramHt.put(ParamsEc2.ParamName.awsSecretKey.toString(),secretAccessKey);
        paramHt.put(ParamsEc2.ParamName.keyName.toString(),rsaKeyName);
        paramHt.put(ParamsEc2.ParamName.rsaKeyPairFile.toString(),
                    rsaKeyPairFile.getPath());
    }
}
