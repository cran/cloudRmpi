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

package com.norbl.cbp.ppe;

import com.norbl.util.*;
import java.util.*;
import java.io.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.*;

/**
 *
 * @author Barnet Wagman
 */
public class UtilEc2 {

    public static String FILE_SEP = SysProp.file_separator.getVal();

        /** Define as the directory that contains lib/mpi-ec2.jar
         *
         * @return
         */
    public static File getMpiEc2Home() {
            //  E.g. /home1/bwd/h/aws/projects/mpi-ec2/lib/mpi-ec2.jar

        // Used for debugging.
        String hdPath = System.getProperty("ppe_home");       
        if ( hdPath != null ) {
            File hd = new File(hdPath);
            if ( hd.exists() && hd.isDirectory() ) return(hd);
        }
        
        String cp = SysProp.java_class_path.getVal();
        String[] jars = cp.split(":");
        
        for ( String jar : jars ) {
            if ( jar.contains("lib" + FILE_SEP + "ppe.jar") )
                return( getHome(new File(jar)) );
        }       
        
            // If we running from the cloudRmpi package, we need a 
            // slightly different test.
        for ( String jar : jars ) {
            if ( jar.contains("jars" + FILE_SEP + "ppe.jar") )
                return( getHome(new File(jar)) );
        }           
        
            // If we are running unit tests in netbeans, there's no
            // jar, so
        for ( String jar : jars ) {
            if ( jar.endsWith("mpi-ec2" + FILE_SEP + "src") )
                return( (new File(jar)).getParentFile() );
            if ( jar.endsWith("ppe_201201" + FILE_SEP + "src") )
                return( (new File(jar)).getParentFile() );
        }

        /* D */ for (String jar : jars ) System.out.println(jar);
        
        throw new RuntimeException("Failed to find mpi-ec2 home.");
    }

    private static File getHome(File jarFile) {
        return(jarFile.getParentFile().getParentFile());
    }

    public static void checkInstanceImageCompatibility(List<Ec2InstanceType> eits,
                                                     InstanceType instanceType,
                                                     String imageID,
                                                     AmazonEC2Client ec2)
        throws FileNotFoundException, IOException, NoSuchInstanceException,
               NoSuchAmiException, IncompatibleInstanceImageException {

        Ec2InstanceType iType = Ec2InstanceType.getEc2InstanceType(eits, instanceType);
        if ( iType == null ) throw new NoSuchInstanceException("Instance type " +
                instanceType + " is not listed in config/ec2-instance-types");

        Image ami = AmiDescription.getImageInfo(ec2, imageID);
        if ( ami == null ) throw new NoSuchAmiException("No ami with ID=" +
                imageID + " was found.");

        if ( iType.isHvm() && !supportsHvm(imageID,ec2) )
            throw new IncompatibleInstanceImageException("Instance type=" +
                    iType.instanceType.toString() + " is not compatible with ami=" +
                    ami + "; this instance type requires an hvm image.");
        else if (iType.isPv() && !supportsPv(imageID,ec2) )
            throw new IncompatibleInstanceImageException("Instance type=" +
                    iType.instanceType.toString() + " is not compatible with ami=" +
                    ami + "; this instance type requires an pv image.");

//        /* L */ UtilEc2.L(instanceType + " and " + imageID + " are compatible.");
    }

    static boolean supportsHvm(String ami,AmazonEC2Client ec2) {
        Image img = AmiDescription.getImageInfo(ec2, ami);
        return( img.getVirtualizationType().toLowerCase().equals("hvm") );
    }

    static boolean supportsPv(String ami,AmazonEC2Client ec2) {
        Image img = AmiDescription.getImageInfo(ec2, ami);
        return( img.getVirtualizationType().toLowerCase().equals("paravirtual") );
    }

    public static void L(String s) {
        System.out.println(s);
    }

    public static String toCsv(List<String> strs) {
        StringBuilder s = new StringBuilder();
        for ( int i = 0; i < strs.size()-1; i++ ) {
            s.append(strs + ", ");
        }
        s.append(strs.get(strs.size()-1));
        return(s.toString());
    }
}
