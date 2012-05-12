/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.amazonaws.*;
import java.io.*;

import com.norbl.cbp.ppe.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.*;
import com.norbl.cbp.ppe.usermonitor.*;
import com.norbl.util.aws.*;
import java.util.*;

/** Holds information about a public AMI that supports ppe. This
 *  supplements the information we can obtain from {@link Image};
 *  note that the AMI tags are not publicly available even
 *  if image is public.<p>
 * 
 *  <tt>AmiPPE</tt>s are stored in S3 (publicly readable).  After
 *  retrieving one, we get its {@link Image} (which is not serializable,
 *  boo, hiss) from AWS.<p>
 * 
 *  This class has static methods for creating, storing and retrien  ving
 *  <tt>AmiPPE</tt>s.
 *
 * @author Barnet Wagman
 */
public class AmiPPE implements Serializable {
    static final long serialVersionUID = ConstantsPPE.SERIAL_VERSION_UID; 
    
    public enum ATag {
        name("Name"),
        nameShort("nameShort"),
        openMPI("Open MPI"),
        r("R"),
        linux("Linux"),
        rreval("rreval"),
        rstudio("RStudio Server"),
        rPackages("R packages")
        ;
        
        public String key;
        ATag(String key) { this.key = key; }
    }
    
    public String amiID;    
    
    /** $ per hour per instance.
     */
    public double imageCharge;
    
    public transient Image image;
    public transient Region region;        
    
    /** AMI tags.  Note that these are only directly accessible by
     *  AMI's owner, even for public AMIs.  We store these in the S3 data.
     */
    public HashMap<String,String> tags;     
    
    /** A description of the AMI used in the tooltip.  This string uses
     *  tag data, so it cannot be build by users.  It is stored in the S3
     *  versions of the AmiPPEs.  This string is html but does not
     *  have enclosing html or body tags.  That way, information can be
     *  appended or prepended to it.  Using an s3 based description et's us
     *  change or reorder the contents with updating the cloudRmpi package.
     */
    public String descriptionHtml;
    
    public AmiPPE(String amiID,double imageCharge) {
        this.amiID = amiID;
        this.imageCharge = imageCharge;
        tags = new HashMap<String, String>();       
    }
     
    public boolean isUsableReBilling(ParamsEc2 params) {        
        if ( imageCharge <= 0.0 ) return(true);
        else return(params.uid  != null);
    }
    
    public String getID() { return(amiID); }
    
    public String getName() {
        if  ( tags != null ) return(tags.get(ATag.name.key));
        else return(" ");
    }
    
    public String getTagVal(ATag aTag) {
        return( tags.get(aTag.key) );   
    }   
        
    /** Note that each ami resides and can be used in one
     *  and only one region (e.g. us-east). 
     * 
     * @return 
     */
    public Region getRegion() { return(region); }
        
        /** Get the availability zones that the specific ami can
         *  run in.  Note that each ami resides and can be used in one
         *  and only one region (e.g. us-east). This method first
         *  determines the region from the ami, and then identifies
         *  the availability zones available in that region to the user.
         */ 
//    public List<AvailabilityZone> getAvailabilityZones() {
//        return(availablityZones);
//    }
    
    public String getVirtualizationType() {
        if ( image != null ) return(image.getVirtualizationType());
        else return(null);
    }
    
    public boolean isParavirtual() {
        
        return( image.getVirtualizationType().equals("paravirtual") );
    }

    public boolean isHvm() {
        return( image.getVirtualizationType().equals("hvm") );
    }
    
    public static class NoImageException extends RuntimeException {
        public NoImageException(String m) {
            super(m);
        }
        public NoImageException() {}
    }
    
//     public boolean isSpecifiedValidAvailabilityZone(String zoneName) {
//        if ( zoneName == null ) return(false);
//        else if (zoneName.equals(ConstantsEc2.ANY_ZONE))
//            return(false);
//        else return( hasZone(zoneName) );
//    }
     
//    private boolean hasZone(String zoneName) {
//        for ( AvailabilityZone z : availablityZones ) {
//            if ( z.getZoneName().equals(zoneName) ) return(true);
//        }
//        return(false);
//    }
    
    private void recordTags(List<Tag> tgs) {
        
        for ( Tag t : tgs ) {            
            tags.put(t.getKey(),t.getValue());                   
        }        
    }
    
    private void createDescriptionHtml() {
    
        StringBuilder s = new StringBuilder();
        
        append(s,ATag.linux,false);
        append(s,ATag.openMPI,true);
        append(s,ATag.r,true);
        append(s,ATag.rreval,true);
        append(s,ATag.rstudio,true);
        append(s,ATag.rPackages,true);
    
        descriptionHtml = s.toString();
    }

    private void append(StringBuilder s, ATag at, boolean prependTagName) {
        
        String val = tags.get(at.key);
        if ( val == null ) return;
        if ( prependTagName ) s.append(at.key + " ");
        s.append(val);
        s.append("<br>");
    }
    
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(amiID + " " + tags.get(ATag.name.key) + " " + imageCharge + " " +
                 region.getRegionName() + " " +
                 getVirtualizationType() + " \n");
        for ( Iterator<String> it = tags.keySet().iterator();
              it.hasNext(); ) {
            String key = it.next();            
            s.append("  " + key + "=" + tags.get(key) + "\n");
        }      
        return(s.toString());
    }
            
        // -------------------------------------------------------------
        // The following methods are for creating ami info in S3 only.
        // -------------------------------------------------------------
        
    public static void getImageInfo(AmazonEC2Client ec2Client,
                                    List<AmiPPE> amis) {
        List<String> amiIDs = new ArrayList<String>();
        for ( AmiPPE ami : amis ) amiIDs.add(ami.amiID);
        
        try {
            DescribeRegionsResult rr = ec2Client.describeRegions();
            for ( Region region : rr.getRegions() ) { 
                ec2Client.setEndpoint(region.getEndpoint());     
                List<AvailabilityZone> azs =
                    ec2Client.describeAvailabilityZones().getAvailabilityZones();
                getImageInfoRegion(ec2Client,region,amis,amiIDs); // ,azs);
            }
        }
        finally { // Reset the endpoint to the default
            ec2Client.setEndpoint("ec2.amazonaws.com"); 
        }
    }
    
    private static void getImageInfoRegion(AmazonEC2Client ec2Client,
                                           Region region,
                                           List<AmiPPE> amis,
                                           List<String> amiIDs
                                           ) {
        DescribeImagesRequest q = new DescribeImagesRequest();
        q = q.withImageIds(amiIDs);
        try {
            DescribeImagesResult r = ec2Client.describeImages(q);
            List<Image> images = r.getImages();
            for ( AmiPPE ap : amis ) {
                Image image = getImage(images,ap.amiID);
                if ( image != null ) {
                    ap.image = image;
                    ap.region = region;
                }
            }
        }
        catch(AmazonServiceException ax) {
        }
    }
    
    private static Image getImage(List<Image> images, String amiID) {
        for ( Image img : images ) {
            if ( img.getImageId().equals(amiID) ) return(img);
        }
        return(null);
    }
    
        /** Warning: this will only work if the images in the AmiPPEs
         *  were retrieved by the the owner of
         *  the images, whether or not they are public.  We run this
         *  once and store the tag info the AmiPPEs in s3.
         * 
         * @param ec2Client
         * @param amis 
         */
    public static void getTags(List<AmiPPE> amis) {    
        for ( AmiPPE ami : amis ) {
            List<Tag> tags = ami.image.getTags();
            ami.recordTags(tags);
        }
    }
    
        /** Warning: this will only work if the images in the AmiPPEs
         *  were retrieved by the the owner of
         *  the images, whether or not they are public.  We run this
         *  once and store the tag info the AmiPPEs in s3.
         * 
         * @param ec2Client
         * @param amis 
         */
    public static void createDescriptions(List<AmiPPE> amis) {    
        for ( AmiPPE ami : amis ) {
            ami.createDescriptionHtml();            
        }
    }
    
    /** Uses {@link #getTags} which only works if the user is the
     *  ami owner.
     * @param ec2Client
     * @param amiIDs
     * @param imageCharge
     * @return 
     */
    public static List<AmiPPE> createAmiPPEs(AmazonEC2Client ec2Client,
                                             String[] amiIDs,
                                             double imageCharge) {
        List<AmiPPE> aps = new ArrayList<AmiPPE>();
        for ( String amiID : amiIDs ) {
            aps.add(new AmiPPE(amiID,imageCharge));
        }
        
        getImageInfo(ec2Client, aps);
        getTags(aps);
        createDescriptions(aps);
        
        return(aps);
    }
    
    /** Uses {@link #getTags} which only works if the user is the
     *  ami owner.
     * 
     * @param ec2Client
     * @param bucketName
     * @param amiIDs 
     */
    public static void createAndUploadAmiPPEs(AmazonEC2Client ec2Client,
                                              S3Access s3Access,                                           
                                              String amiGroupName,
                                              String[] amiIDs,
                                              double imageCharge) {
        List<AmiPPE> aps = createAmiPPEs(ec2Client, amiIDs, imageCharge);
        
        for ( AmiPPE ap : aps ) System.out.println(ap);
            
        s3Access.putObject(ConstantsUM.PPE_INFO_BUCKET_NAME,amiGroupName,
                           (Serializable) aps,
                           true);
    }
    
    public static void uploadAmiPPEs(S3Access s3Access,                                           
                                     String amiGroupName,
                                     List<AmiPPE> amiPPEs) {
        s3Access.putObject(ConstantsUM.PPE_INFO_BUCKET_NAME,amiGroupName,
                           (Serializable) amiPPEs,
                           true);
    }
    
    public static void createAndUploadAmiPPEs(String amiGroupName,
                                              String[] amiIDs,
                                              double imageCharge)
        throws Exception {
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        
        ParamHtPPE pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pec2 = new ParamsEc2(pht);
        
        AmazonEC2Client ec2Client = 
                new AmazonEC2Client(pec2.buildAWSCredentials());
        
        AmazonS3Client s3Client = 
                new AmazonS3Client(pec2.buildAWSCredentials());
        S3Access s3a = new S3Access(s3Client);
        
        createAndUploadAmiPPEs(ec2Client, s3a, amiGroupName, amiIDs, imageCharge);
    }
    
    public static List<AmiPPE> retrieveAmiPPEs(S3Access s3Access,                                             
                                               String amiGroupName) {
        return( (List<AmiPPE>) s3Access.getObject(ConstantsUM.PPE_INFO_BUCKET_NAME,
                 amiGroupName) );
    }
    
    public static List<AmiPPE> retrieveAndInitAmiPPEs(S3Access s3Access,   
                                                      String amiGroupName,
                                                      AmazonEC2Client aec) {
        List<AmiPPE> amis = retrieveAmiPPEs(s3Access, amiGroupName);           
        List<String> IDs = new ArrayList<String>();
        for ( AmiPPE ami : amis ) {
            IDs.add(ami.amiID);          
        }
        
        DescribeImagesRequest req = new DescribeImagesRequest();
        req.setImageIds(IDs);
        
        DescribeImagesResult r = aec.describeImages(req);
        List<Image> images = r.getImages();
      
        for ( Image img : images ) {                       
            AmiPPE ami = getForID(amis,img.getImageId());         
            if ( ami != null ) ami.image = img;                           
        }
        
        Collections.sort(amis,
            new Comparator<AmiPPE>() {
                public int compare(AmiPPE a, AmiPPE b) {
                    if ( a.imageCharge > b.imageCharge ) return(-1);
                    else if ( a.imageCharge < b.imageCharge ) return(1);
                    else return(0);
                }
            }
        );
        
        return(amis);
    }
    
    private static AmiPPE getForID(List<AmiPPE> amis, String ID) {
        for ( AmiPPE ami : amis ) {
            if ( ami.amiID.equals(ID) ) return(ami);
        }
        return(null);
    }
    
    public static void createAmiPPEs(String amiGroupName, 
                                     String[] amiIDs,
                                     double imageCharge) 
        throws Exception {
               
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        
        ParamHtPPE pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pec2 = new ParamsEc2(pht);
        
        AmazonEC2Client ec2Client = 
                new AmazonEC2Client(pec2.buildAWSCredentials());
        
        AmazonS3Client s3Client = 
                new AmazonS3Client(pec2.buildAWSCredentials());
        S3Access s3a = new S3Access(s3Client);
        
        List<AmiPPE> aps = createAmiPPEs(ec2Client, amiIDs, imageCharge);
        for (AmiPPE ap : aps ) System.out.println(ap);
        
        ec2Client.shutdown();
    }
}