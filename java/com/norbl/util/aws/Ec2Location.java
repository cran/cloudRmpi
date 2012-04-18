/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.util.aws;

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import com.norbl.cbp.ppe.*;

/**
 *
 * @author Barnet Wagman
 */
public class Ec2Location {

    // These are all ec2 regions as of 4/2012. They can be obtained
    // programmtically and can changes.  These strings are use to
    // get a specific region object and should not be buried too
    // deep in the code.
    public static final String EU = "eu-west-1";
    public static final String SA = "sa-east-1";
    public static final String US_EAST = "us-east-1";
    public static final String AP_NE = "ap-northeast-1";
    public static final String US_WEST_2 = "us-west-2";
    public static final String US_WEST_1 = "us-west-1";
    public static final String AP_SE = "ap-southeast-1";
    
    public static List<Region> getAllRegions(AmazonEC2Client aec) {
               
            DescribeRegionsResult rr = aec.describeRegions();
            return( rr.getRegions() );      
    }
    
    public static Region getRegion(AmazonEC2Client aec, String regionName) {
        
        List<Region> regions = getAllRegions(aec);
        for (Region r : regions ) {
            if ( r.getRegionName().equals(regionName) ) return(r);
        }
        return(null);
    }
    
    /** Tests whether zone is valid for this ec2 client. 
     * 
     * @param aec
     * @param zone
     * @return 
     */
    public static boolean isValidAvailablityZone(AmazonEC2Client aec,
                                                 String zoneName) {
        DescribeAvailabilityZonesResult r =  aec.describeAvailabilityZones();
        for ( AvailabilityZone z : r.getAvailabilityZones() ) {
            if ( z.getZoneName().equals(zoneName) ) return(true);
        }
        return(false);
    }   
    
    
    public static void main(String[] argv) throws Exception {
        
        ParamHtPPE paramsHt = new ParamHtPPE(new String[] {});
        ParamsEc2 paramsEc2 = new ParamsEc2(paramsHt);
        Ec2Wrangler w = new Ec2Wrangler(paramsEc2,null);
        
//        List<Region> lr = getAllRegions(w.ec2Client);
//        for ( Region r : lr ) System.out.println(r.getRegionName());
        Region r = getRegion(w.ec2Client, Ec2Location.US_EAST);
        System.out.println(r.getRegionName() + " " + r.toString());
        
//        Region r = new Region();
//        r.setRegionName("us-east-1");
//        System.out.println(r.getEndpoint());
//        List<AvailabilityZone> zs = getAvailabilityZones(w.ec2Client, r);
//        for ( AvailabilityZone z : zs )
//            System.out.println("az=" + z.getZoneName());
    }
    
    
}
