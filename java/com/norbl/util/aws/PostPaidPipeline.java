 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.util.aws;

import com.amazonaws.cbui.*;
import java.util.*;

/** There is no 'PostPaid' pipeline in the aws fps library.  This is
 *  my version, based on {@link AmazonFPSMultiUsePipeline}
 *  and information from aws employee on forum (which could, of course,
 *  be wrong.
 *
 * @author Barnet Wagman
 */
public class PostPaidPipeline extends AmazonFPSCBUIPipeline {
    
    public PostPaidPipeline(String accessKey, String secretKey) {
        super("SetupPostpaid", accessKey, secretKey);
        //    ^ Case sensitve
    }
    
    /** Sets all the parameters want to use.  This includes all
     *  'mandatory' and params we either want or think we may need.
     * 
     */
    public void setParameters(String returnURL,
                              String callerReference,
                              String callerReferenceSender,
                              String callerReferenceSettlement,
                              String globalAmountLimit,
                              String creditLimit,
                              String paymentReason,
                              String currencyCode,
                              String paymentMethod,
                              String validityExpiry
                              ) {
        addParameter("returnURL",returnURL);
        addParameter("callerReference",callerReference);
        addParameter("callerReferenceSender",callerReferenceSender);
        addParameter("callerReferenceSettlement",callerReferenceSettlement);
        addParameter("globalAmountLimit",globalAmountLimit);
        addParameter("creditLimit",creditLimit);
        addParameter("paymentReason",paymentReason);
        addParameter("currencyCode",currencyCode);
        addParameter("paymentMethod",paymentMethod);
        addParameter("validityExpiry",validityExpiry);        
    }

    protected void validateParameters(Map<String, String> parameters) {
        
        for ( Iterator<String> it = parameters.keySet().iterator();
              it.hasNext(); ) {
            String key = it.next();
            String val = parameters.get(key);
            if ( val == null ) 
                throw new RuntimeException("null value for parameter " + key);
        }        
    }
}
