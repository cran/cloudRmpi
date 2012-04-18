/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.amazonaws.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.*;
import com.norbl.cbp.ppe.*;
import com.norbl.cbp.ppe.ompi.*;
import com.norbl.cbp.ppe.usermonitor.*;
import com.norbl.util.*;
import com.norbl.util.aws.*;
import com.norbl.util.gui.*;
import java.io.*;
import java.util.*;

/** Static methods (an main) for writing data to s3, etc.  These are not
 *  used by users.
 *
 * @author Barnet Wagman
 */
public class UtilApps {
    
    public static void main(String[] argv) throws Exception {
        
        // AMIs
//        createOmpiAmiPPEsInS3();
//        createCloudRmpiAmiPPEsInS3();
                
               
        // Instance types        
//        writeInstancesTypesToS3();
//        System.out.println("Wrote eits to s3");
//        List<Ec2InstanceType> eits = Ec2InstanceType.getInstanceTypes();
//        for ( Ec2InstanceType eit : eits ) System.out.println(eit);
        
        // View amis
//        viewAmis(ConstantsPPE.OMPI_AMI_GROUP);
        viewAmis(ConstantsPPE.CLOUDRMPI_AMI_GROUP);
        
//        ParamHtPPE ph = new ParamHtPPE(new String[] {});
//        ParamsEc2 pe3 = new ParamsEc2(ph);
//        AmazonEC2Client aec =  new AmazonEC2Client(pe3.buildAWSCredentials());
//        
//        S3Access s3a = new S3Access();
//        
//        List<AmiPPE> amis = AmiPPE.retrieveAmiPPEs(s3a,"ppe-cloudrmpi-ami-zc");
//        System.out.println("n ami=" + amis.size());
//        for ( AmiPPE ami : amis ) {
//            System.out.println(ami.amiID  );
//            for ( Iterator<String> it = ami.tags.keySet().iterator(); it.hasNext(); ) {
//                String key = it.next();
//                System.out.println("    " + key + "=" + ami.tags.get(key));
//            }
//                     
//        }
        
    }

    // ---------- AMI info ------------------------------------
 
    // This section has the definitive apps for putting ami info into S3.
    // One of these should be modified and run whenever an ami is creates,
    // delete, or if there is price change.
    
    public static void createOmpiAmiPPEsInS3() throws Exception {
        
        String groupName = ConstantsPPE.OMPI_AMI_GROUP;
        
        AmazonEC2Client ec2Client = getEc2ClientNBL();
        AmazonS3Client s3Client = getS3ClientNBL();
        S3Access s3a = new S3Access(s3Client);
        
        List<AmiPPE> zcs = AmiPPE.createAmiPPEs(ec2Client,
                            new String[] { "ami-cc629ba5", "ami-eb478182" },
                            0.0);
        
        List<AmiPPE> cs = AmiPPE.createAmiPPEs(ec2Client,
                            new String[] { 
            "ami-7039e219", //  ppe-ompi v1.2 pv   
            "ami-3237ec5b"  //  ppe-ompi v1.2 hvm
                            },
                            ConstantsUM.PRICE_PIPH_DOUBLE);
        
        List<AmiPPE> amis = new ArrayList<AmiPPE>();
        amis.addAll(zcs);
        amis.addAll(cs);
        
        AmiPPE.uploadAmiPPEs(s3a, groupName, amis);
        
        System.out.println("Uploaded " + amis.size() + " AmiPPE to s3 object " +
                           groupName);
    }
    
    public static void createCloudRmpiAmiPPEsInS3() throws Exception {
        
        String groupName = ConstantsPPE.CLOUDRMPI_AMI_GROUP;
        
        AmazonEC2Client ec2Client = getEc2ClientNBL();
        AmazonS3Client s3Client = getS3ClientNBL();
        S3Access s3a = new S3Access(s3Client);
        
        List<AmiPPE> zcs = AmiPPE.createAmiPPEs(ec2Client,
                                        new String[] { 
            "ami-5608d73f", 
            "ami-58bb6431"
                                         },
                            0.0);                
        
        List<AmiPPE> cs = AmiPPE.createAmiPPEs(ec2Client,
                            new String[] { 
            "ami-5a04df33",  // cloudRmpi pv 1.1       
            "ami-dc30ebb5"   // cloudRmpi hmv 1.1                      
                            },
                            ConstantsUM.PRICE_PIPH_DOUBLE);
        
        List<AmiPPE> amis = new ArrayList<AmiPPE>();
        amis.addAll(zcs);
        amis.addAll(cs);
        
        AmiPPE.uploadAmiPPEs(s3a, groupName, amis);
        
        System.out.println("Uploaded " + amis.size() + " AmiPPE to s3 object " +
                           groupName);
    }
    
    // -------------------------------------------------------
    
    public static void viewAmis(String groupName) {
        
        S3Access s3a = new S3Access();
        
        List<AmiPPE> amis = AmiPPE.retrieveAmiPPEs(s3a,groupName);
        System.out.println("n ami=" + amis.size());
        for ( AmiPPE ami : amis ) {
            System.out.println(ami.amiID  );
            for ( Iterator<String> it = ami.tags.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                System.out.println("    " + key + "=" + ami.tags.get(key));
            }
                     
        }
    }
    
    public static List<AmiPPE> getAmiData() {        
        List<AmiPPE> amis = new ArrayList<AmiPPE>();
        
        S3Access s3a = new S3Access();
        
        
        for ( String gn : 
              new String[] {        
                "ppe-runjrun-ami-zc",
                "ppe-cloudrmpi-ami-zc"
              } ) {
        
            amis.addAll(// AmiPPE.retrieveAndInitAmiPPEs(s3a,gn,ec2Client));
                        AmiPPE.retrieveAmiPPEs(s3a,gn));
        }
        return(amis);
    }
    
    // -------------------------------------------------------
    
    public static void createOmpiZCs() throws Exception {
        
        AmiPPE.createAndUploadAmiPPEs("ppe-ompi-ami-zc",
                       new String[] { "ami-cc629ba5", "ami-eb478182" },
                       0.0);                
    }
    
    public static void createCloudRmpiZCs() throws Exception {
        
        AmiPPE.createAndUploadAmiPPEs("ppe-cloudrmpi-ami-zc",
                       new String[] { "ami-5608d73f", "ami-58bb6431"},           
                       0.0);             
    }
    
    public static void createRunJRunZCs() throws Exception {
        
        AmiPPE.createAndUploadAmiPPEs("ppe-runjrun-ami-zc",
                       new String[] { "ami-37836d5e" },           
                       0.0);             
    }
    
    
    
    public static AmazonEC2Client getEc2ClientNBL()
        throws Exception {
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        
        ParamHtPPE pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pec2 = new ParamsEc2(pht);
        
        return(new AmazonEC2Client(pec2.buildAWSCredentials()));                        
    }
    
    public static AmazonS3Client getS3ClientNBL()
        throws Exception {
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        
        ParamHtPPE pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pec2 = new ParamsEc2(pht);
        
        return(new AmazonS3Client(pec2.buildAWSCredentials()));                
    }
    
    public static SDBAccess getSDBAccess()
        throws Exception {
        String cf = "/home/moi/eh/aws/nbl_account/nbl.ppe-config";
        
        ParamHtPPE pht = new ParamHtPPE(new String[] {"configFile=" + cf});
        ParamsEc2 pec2 = new ParamsEc2(pht);
        
        return(new SDBAccess(pec2));                
    }
    
    // --------- Instance types -----------------------------
    
    public static void writeInstancesTypesToS3() throws Exception {
        
        ParamHtPPE paramsHt = new ParamHtPPE(new String[] {
            ParamHt.ParamName.configFile.toString() + "=" +
                "/home/moi/eh/aws/nbl_account/nbl.ppe-config"
        } );
        ParamsEc2 paramsEc2 = new ParamsEc2(paramsHt);
                
        S3Access s3a = new S3Access(paramsEc2);
        
        List<Ec2InstanceType> eits = getInstanceTypesFromFile();
        System.out.println("Read " + eits.size() + " from file.");
        
        s3a.putObject(ConstantsUM.PPE_INFO_BUCKET_NAME,
                      ConstantsUM.INSTANCE_TYPE_LIST_KEY, 
                      (Serializable) eits,
                      true);        
    }
    
    public static List<Ec2InstanceType> getInstanceTypesFromFile() 
        throws FileNotFoundException, IOException, NullPointerException {        
        
        File f = new File("/home/moi/h/ppe/data_files/ec2-instance-types");
        return( getInstanceTypesFromFile(f) );        
    }
    
       /** Gets a list of supported ec2 instance types from a local file.
        *   This is used to create the s3 based list.  It is not used by
        *   users.
         *  
         * @return
         */
    private static List<Ec2InstanceType> getInstanceTypesFromFile(File f)
        throws FileNotFoundException, IOException, NullPointerException {
        
        if ( !f.exists() ) throw new RuntimeException("The instance type file, " +
                f.getPath() + " does not exist.");
        List<String[]> lines = FileUtil.readConfigTypeFile(f,"[ \t]+","#",7);
              
        List<Ec2InstanceType> iTypes = new ArrayList<Ec2InstanceType>();
       
        for ( String[] ln : lines ) {                   
            try {
            InstanceType instanceType = Ec2InstanceType.getInstanceType(ln[0]);
            iTypes.add(
                new Ec2InstanceType(
                    instanceType,// ln[0],
                    Integer.parseInt(ln[1]), // n cores
                    Double.parseDouble(ln[2]), // RAM GM
                    toBoolean(ln[3]), // custer 
                    Ec2InstanceType.VirtualizationType.valueOf(ln[4]),    // vt
                    Double.parseDouble(ln[5]),
                    ln[6] // description
                    )
                );    
            }
            catch(IllegalArgumentException ix) {
                GuiUtil.warning(new String[] {
                    "Undefined instance type: " + ln[0],
                    "it will be omitted.",
                    ix.getMessage() },
                "Undefined instance type");                        
            }
        }
        return(iTypes);
    }
    
    static private boolean toBoolean(String s) {
        return( "yes".equals(s.trim().toLowerCase()) );
    }
}