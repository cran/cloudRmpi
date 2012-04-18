/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.util.aws;

import com.amazonaws.*;
import java.io.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.norbl.util.*;
import com.norbl.cbp.ppe.*;

/**
 *
 * @author Barnet Wagman
 */
public class S3Access {

    ParamsEc2 paramsEc2;
    AmazonS3Client s3Client;
            
    public S3Access(ParamsEc2 paramsEc2) throws MissingParamsException {
        this.paramsEc2 = paramsEc2;       
        s3Client = new AmazonS3Client(paramsEc2.buildAWSCredentials());
    }
    
    public S3Access(AmazonS3Client s3Client) {      
        this.s3Client = s3Client;
    }
    
    /** For anonymous access only.
     * 
     */
    public S3Access() {
        s3Client = new AmazonS3Client();
    }
    
    public void close() { s3Client.shutdown(); }
    
    /** Overwrites if an object with the specified key already exists.
     * 
     * @param bucketName
     * @param key
     * @param obj 
     */
    public synchronized void putObject(String bucketName,String key,Serializable obj,
                                       boolean publicReadable) {
        
            // This is truly insipid.
        ByteArrayInputStream bis = null;
        try {
            byte[] b = objectToByteArray(obj);
            bis = new ByteArrayInputStream(b);        
            ObjectMetadata omd = new ObjectMetadata();
            omd.setContentType("application/octet-stream");
            omd.setContentLength(b.length);
            PutObjectRequest r = new PutObjectRequest(bucketName,key,bis,omd);            
            s3Client.putObject(r);     
            if ( publicReadable ) 
                s3Client.setObjectAcl(bucketName, key, 
                                      CannedAccessControlList.PublicRead);
        }
        finally {
            try {
                if ( bis != null ) bis.close();
            }
            catch(Exception xxx) { System.err.println(xxx); }
        }
    }
    
    /** If the object does not exist, null is returned.
     * 
     * @param bucketName
     * @param key
     * @return the object or null if it does not exist.
     */
    public synchronized Object getObject(String bucketName,String key) {
        try {
            GetObjectRequest r = new GetObjectRequest(bucketName,key);
            S3Object s3o = s3Client.getObject(r);
            S3ObjectInputStream sois =  s3o.getObjectContent();
            ObjectInputStream ois = new ObjectInputStream(sois);
            return( ois.readObject() );
        }
        catch(AmazonServiceException asx) { return(null); }
        catch(IOException iox) {
            System.err.println(iox);
            return(null);
        }
        catch(ClassNotFoundException cx) {
            System.err.println(cx);
            return(null);
        }
    }
    
    
        // ---------------------------------------------------
    
    private static byte[] objectToByteArray(Object obj ) {

        try {                               
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();	 
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(obj);            
	    // oos.flush();      
            oos.reset();
            byte[] b = baos.toByteArray();                        
            baos = null;            
            /* ^^^^^^^^ WARNING-Java BUG! when serializing large objects
             * the baos seems to leak memory.  When this method returns
             * baos is unreferenced and SHOULD get all its space freed up,
             * but it does not appear to, unless we explicitly null it out.
             */
            return(b);           
	}        
	catch (java.io.IOException iox) {
            throw new RuntimeException(iox);
	}
    } 
    
    private static Object byteArrayToObject(byte[] B ) {
	try {
	    ByteArrayInputStream baos = new ByteArrayInputStream(B,0,B.length);	   	    
	    Object obj = new Object();            
            ObjectInputStreamWithPrimitiveSupport oos 
                = new ObjectInputStreamWithPrimitiveSupport(baos);	
	    obj = oos.readObject();
	    oos.close();
            baos = null;
    
	    return(obj);
	}
        catch(InvalidClassException xc) { throw new RuntimeException(xc); }
	catch ( java.io.IOException iox ) { throw new RuntimeException(iox); }	
	catch ( ClassNotFoundException cnfx ) { throw new RuntimeException(cnfx); }
    }    
    
    private static class ObjectInputStreamWithPrimitiveSupport 
        extends ObjectInputStream {
        
        public ObjectInputStreamWithPrimitiveSupport(InputStream is) 
            throws IOException, StreamCorruptedException {
            super(is);
        }

        public Class resolveClass(ObjectStreamClass desc)
            throws ClassNotFoundException, IOException {             
            try {
                return super.resolveClass(desc);
            } catch (ClassNotFoundException e) {
                String nm = desc.getName();
                if (nm.equals("int")) return int.class;
                if (nm.equals("long")) return long.class;
                if (nm.equals("float")) return float.class;
                if (nm.equals("double")) return double.class;
                if (nm.equals("char")) return char.class;
                if (nm.equals("boolean")) return boolean.class;
                if (nm.equals("short")) return short.class;
                if (nm.equals("byte")) return byte.class;
                else /* check for other primitive types if one wishes.*/
                    throw e;
            }
        }
    }    
}
