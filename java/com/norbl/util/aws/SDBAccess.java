/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.util.aws;

import com.norbl.util.*;
import com.norbl.cbp.ppe.*;
import com.amazonaws.services.simpledb.*;
import com.amazonaws.services.simpledb.model.*;
import java.util.*;

/** 
 *
 * @author Barnet Wagman
 */
public class SDBAccess {

    ParamsEc2 paramsEc2;
    AmazonSimpleDBClient sdbClient;
    
     public SDBAccess(ParamsEc2 paramsEc2) throws MissingParamsException {
        this.paramsEc2 = paramsEc2;
        sdbClient = new AmazonSimpleDBClient(paramsEc2.buildAWSCredentials());
     }
     
     public synchronized SelectResult getAll(String domain) {         
         return( sdbClient.select(new SelectRequest("select * from " + domain)) );
     }
     
     /** Assumes that the keyVal is unique - yields at most one item.
      *  If an item with the key val exists, the sought attribute is
      *  assumed to have only one value.  If multiple items or values
      *  are found, an exception is thrown.  If the no item has the
      *  key, null is returned.
      * 
      * @param keyName 
      * @param keyVal
      * @param attributeSought
      * @return 
      */
     public synchronized String getUVal(String domain,
                                         String keyAttributeName, 
                                         String keyAttributeVal, 
                                         String soughtAttributeName) 
         throws SDBAccessException {
         
         SelectResult r = 
            sdbClient.select(
                new SelectRequest(
                    "select " + soughtAttributeName + 
                    " from " + domain + 
                    " where " +
                        keyAttributeName.toString() + " = " +
                        "'" + keyAttributeVal + "'",
                true));
               
        List<Item> items = r.getItems();
        if ( items.size() == 1 ) {
            List<Attribute> atts = items.get(0).getAttributes();
            if ( atts.size() == 1 ) {
                return(atts.get(0).getValue());
            }
            else if ( atts.size() == 0 ) return(null);
            else throw new SDBAccessException(
                keyAttributeName.toString() + "=" + keyAttributeVal + 
                " has " + atts.size() + " attributes.");
        }
        else if ( items.size() < 1 ) return(null);
        else throw new SDBAccessException(
                keyAttributeName.toString() + "=" + keyAttributeVal + 
                " has " + items.size() + " items.");       
     }
     
     /** Overwrites existing attribute values.
      * 
      * @param domain
      * @param itemName
      * @param nats 
      */
     public synchronized void addAttributes(String domain,
                                            String itemName,
                                            List<NamedAttribute> nats) {
         List<ReplaceableAttribute> rats = 
                 new ArrayList<ReplaceableAttribute>();
         
         for ( NamedAttribute na : nats ) {
             rats.add(new ReplaceableAttribute(na.name,na.val,true));
         }
         
         sdbClient.putAttributes(new PutAttributesRequest(domain,itemName,rats));
     }
     
     public static class NamedAttribute {
         public String name;
         public String val;
         
         public NamedAttribute(String name,String val) {
             this.name = name;
             this.val = val;
         }
     }
     
     public synchronized void addAttribute(String domain, String itemName,
                                           String attributeName,
                                           String attributeVal) {
         List<ReplaceableAttribute> rats = 
                 new ArrayList<ReplaceableAttribute>();
         rats.add(new ReplaceableAttribute(attributeName,
                                           attributeVal,
                                           true));
         sdbClient.putAttributes(new PutAttributesRequest(domain,itemName,rats));     
     }
}
