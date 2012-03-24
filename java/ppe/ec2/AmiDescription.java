/*
    Copyright 2011 Northbranchlogic, Inc.

    This file is part of Parallel Processing with EC2 (ppe).

    ppe is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ppe is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package ppe.ec2;

import com.amazonaws.AmazonServiceException;
import ppe.*;
import java.io.*;
import java.util.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.ArrayList;

/** Static methods for getting information about amis.
 *
 * @author Barnet Wagman
 */
public class AmiDescription {   
        /**
         *
         * @param ec2Client
         * @param amiID
         * @return null if amiID does not exist.
         */
    public static Image getImageInfo(AmazonEC2Client ec2Client,
                                     String amiID) {
        List<String> IDs = new ArrayList<String>();
        IDs.add(amiID);
        DescribeImagesRequest req = new DescribeImagesRequest();
        req.setImageIds(IDs);

        List<Image> imgs = ec2Client.describeImages(req).getImages();
        for ( Image img : imgs ) {
            if ( img.getImageId().equals(amiID) ) return(img);
        }
        return(null);
    }

        /** Get the availability zones that the specific ami can
         *  run in.  Note that each ami resides and can be used in one
         *  and only one region (e.g. us-east). This method first
         *  determines the region from the ami, and then identifies
         *  the availability zones available in that region to the user.
         *
         * @param ec2Client
         * @param amiID
         * @return
         */
    public static List<AvailabilityZone> getAvailabilityZones(
                                                AmazonEC2Client ec2Client,
                                                Region region) {

        ec2Client.setEndpoint(region.getEndpoint());
        return( ec2Client.describeAvailabilityZones().getAvailabilityZones() );
    }

    public static List<AvailabilityZone> getAvailabilityZones(
                                                    AmazonEC2Client ec2Client,
                                                    String amiID) {
        Region region = getRegion(ec2Client, amiID);
        if ( region == null ) return(new ArrayList<AvailabilityZone>());
        else return(getAvailabilityZones(ec2Client, region));
    }

    public static Region getRegion(AmazonEC2Client ec2Client,String amiID) {

        DescribeRegionsResult rr = ec2Client.describeRegions();
        for ( Region reg : rr.getRegions() ) {            
            ec2Client.setEndpoint(reg.getEndpoint());           
            if ( hasAmi(ec2Client,amiID) ) {
                return(reg);
            }
        }
            // Reset the endpoint to the default
        ec2Client.setEndpoint("ec2.amazonaws.com");

        return(null);

    }

        /**
         *
         * @param ec2Client
         * @param zoneName
         * @return the zone that matches zoneName or <tt>null</tt>, which
         *  indicates that the zone is unspecified and any zone may be used.
         */
    public static AvailabilityZone getAvailabilityZone(AmazonEC2Client ec2Client,
                                           String zoneName) {
        DescribeAvailabilityZonesResult r =  ec2Client.describeAvailabilityZones();
        for ( AvailabilityZone z : r.getAvailabilityZones() ) {
            if ( z.getZoneName().equals(zoneName) ) return(z);
        }
        return(null);
    }

    public static boolean isSpecifiedValidAvailabilityZone(AmazonEC2Client ec2Client,
                                                           String zoneName) {
        if ( zoneName == null ) return(false);
        else if(zoneName.equals(ppe.gui.networkspec.AvailabilityZoneChoice.ANY_ZONE))
            return(false);
        else return( getAvailabilityZone(ec2Client,zoneName) != null );
    }

    public static boolean hasAmi(AmazonEC2Client ec2Client,String amiID) {
        try {
            DescribeImagesRequest q = new DescribeImagesRequest();
            List<String> amis = new ArrayList<String>();
            amis.add(amiID);
            q = q.withImageIds(amis);
            DescribeImagesResult r = ec2Client.describeImages(q);
            return( r.getImages().size() > 0 );
        }
        catch(AmazonServiceException xxx) { return(false); }
    }


    public static List<String> getSupportedAmiIDs()
        throws FileNotFoundException, IOException {

//        File dir = new File(UtilEc2.getMpiEc2Home(),Constants.CONFIG_DIR_NAME);
//        if ( !dir.exists() ) dir.mkdirs();
//        File f = new File(dir,Constants.AMI_ID_FILENAME);
        File f = getAmiIDFile();
      
        List<String[]> lines = UtilPPE.readConfigTypeFile(f,"[ \t]+","#");
       
        List<String> names = new ArrayList<String>();

        for ( String[] ln : lines ) {
            if ( (ln.length >= 1) && (ln[0] != null) &&
                 (ln[0].trim().length() > 0) )
            names.add(ln[0]);
        }
        return(names);
    }
    
    public static File getAmiIDFile() {
        File dir = new File(UtilEc2.getMpiEc2Home(),Constants.CONFIG_DIR_NAME);
        if ( !dir.exists() ) dir.mkdirs();
        return( new File(dir,Constants.AMI_ID_FILENAME) );
    }

    public static List<Image> getSupportedImages(AmazonEC2Client ec2Client)
        throws FileNotFoundException, IOException {

        List<String> amiIDs = getSupportedAmiIDs();
        List<Image> images = new ArrayList<Image>();
        for ( String ai : amiIDs ) {
            Image img = getImageInfo(ec2Client, ai);
            if ( img != null ) images.add(img);
        }
        return(images);
    }

    public static boolean isParavirtual(Image img) {
        return( img.getVirtualizationType().equals("paravirtual") );
    }

    public static boolean isHvm(Image img) {
        return( img.getVirtualizationType().equals("hvm") );
    }
}
