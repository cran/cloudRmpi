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

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.usermonitor.*;
import java.util.*;
import java.io.*;
import com.norbl.util.gui.*;
import com.norbl.util.*;
import com.norbl.util.aws.*;


/** Some descriptive information about ec2 instance types. This
 *  class contains static functions for getting the list of 
 *  {@link Ec2InstanceType}s from s3 (and putting them there).
 *
 * @author Barnet Wagman
 */
public class Ec2InstanceType implements TableModelRowable, Serializable {
    static final long serialVersionUID = 0L;       

    public static enum VirtualizationType {
        pv, // paravirtual;
        hvm // Hardware Virtual Machine
    }

    public InstanceType instanceType;
    public VirtualizationType vt;
    public int nCores;
    public double ramGB;
    public boolean clusterSupport;
    public double pricePiph;
    public String description;
    
     public Ec2InstanceType(// String name,
                            InstanceType instanceType,
                            int nCores,
                            double ramGB,
                            boolean clusterSupport,
                            VirtualizationType vt,
                            double pricePiph,
                            String description) {
         this.instanceType = instanceType;
         this.nCores = nCores;
         this.ramGB = ramGB;
         this.clusterSupport = clusterSupport;
         this.vt = vt;
         this.pricePiph = pricePiph;
         this.description = description;
     }
   
    public String toString() { return(instanceType.toString() + " " + 
                                      vt.toString() +
                                      " nCores=" + Integer.toString(nCores) +
                                      " ramGB=" + StringUtil.f1(ramGB) +
                                      " cluster support=" + Boolean.toString(clusterSupport) +
                                      " price/instance/hr=" + StringUtil.f2(pricePiph) +
                                      " " + description);
    }
                                               

    public boolean isPv() {
        return( vt.equals(VirtualizationType.pv));
    }

    public boolean isHvm() {
        return( vt.equals(VirtualizationType.hvm));
    }
    
    public String getDescription() { return(description); }
       
    /** NOTE/WARNING: there is a bug in the aws implementation of
     *  {@link InstanceType#valueOf(java.lang.String) }.
     *  It throws an exception on t1.micro even if it obtained from 
     *  {@link InstanceType#values() }.toString(). They've added a kluge method
     *  {@link InstanceType#fromValue(java.lang.String) } that solves the problem.
     *  That method is used in this function.  All instance type translation should
     *  be done with this method only.
     * 
     * @param instanceTypeName
     * @return
     * @throws IllegalArgumentException 
     */
    public static InstanceType getInstanceType(String instanceTypeName) 
        throws IllegalArgumentException {
        return(InstanceType.fromValue(instanceTypeName));
    }
    
    public static Ec2InstanceType getEc2InstanceType(
                                                List<Ec2InstanceType> eits,
                                                InstanceType iType) 
        throws FileNotFoundException, IOException, NullPointerException {
        
        for ( Ec2InstanceType eit : eits ) {
            if ( eit.instanceType.equals(iType) ) return(eit);
        }
        return(null);
    }
    
    static private boolean toBoolean(String s) {
        return( "yes".equals(s.trim().toLowerCase()) );
    }
    
//    public static File getInstanceTypeFile() {
//        File dir = new File(UtilEc2.getMpiEc2Home(),ConstantsEc2.CONFIG_DIR_NAME);
//        if ( !dir.exists() ) dir.mkdirs();
//        return(new File(dir,ConstantsEc2.INSTANCE_TYPE_FILENAME));
//    }
   
//    public static int getNCores(InstanceType instanceType)
//        throws FileNotFoundException, IOException {
//
//        Ec2InstanceType eit = getInstanceInfo(instanceType);
//        if ( eit != null ) return(eit.nCores);
//        else return(-1);
//    }

        /** There may be an inconsistency in AWS's strings for virtualization
         *  type.  It may be that both 'pv' and 'paravitual' are used. So
         */
    public static boolean isPv(String s) {
        if ( s == null ) return(false);
        String l = s.trim().toLowerCase();
        return( l.equals("pv") || l.equals("paravirtual") );
    }

    public static boolean isHvm(String s) {
        if ( s == null ) return(false);
        String l = s.trim().toLowerCase();
        return( l.equals("hvm") || l.equals("hardware virtual machine") ||
                l.equals("hardwarevirtualmachine") );
    }
    
        // ------------------------------------------------------

    public Class getColumnClass(int colIndex) {
        switch(colIndex) {
            case 0: return(String.class);
            case 1: return(String.class);
            case 2: return(Integer.class);
            case 3: return(String.class);
            default: return(null);
        }
    }
 
    public String getColumnName(int colIndex) {
        switch(colIndex) {
            case 0: return("Name");
            case 1: return("Virtualization type");
            case 2: return("N cores");
            case 3: return("Description");
            default: return(null);
        }
    }
   
    public Object getColumnValue(int colIndex) {
       switch(colIndex) {
            case 0: return((instanceType != null)?instanceType.toString():"");
            case 1: return((vt != null)?vt.toString():"");
            case 2: return(new Integer(nCores));
            case 3: return((description != null)?description:"");
            default: return(null);
        }
    }
   
    public int getNColumns() { return(4); }      
    
    public void setColumnValue(int colIndex, Object value) {
        switch(colIndex) {
            case 0:
                if ( value instanceof String) {
                    String nm = ((String) value).trim();
                    try {
                        instanceType = Ec2InstanceType.getInstanceType(nm);
                    }
                    catch(Exception itx) {
                        warning("Undefined instance type: " + nm);
                        instanceType = null;
                    }
                    
//                    if ( !UtilPPE.containsWhiteSpace(nm) ) name = nm;
//                    else warning(nm + " contains whitespace, which is not " +
//                                "allowed in instance type names.");
                }                                         
                else warning(" Illegal " + getColumnName(colIndex) +
                            "; it must be a string.");
                break;                
            case 1:
                VirtualizationType nvt = getVT(value);
                if ( nvt != null ) vt = nvt;
                else warning("Undefined virtualization type=" + value +
                             "; it must be pv or hvm");
                break;
            case 2: 
                int nc = getNC(value);
                if ( nc > 0 ) nCores = nc;
                else warning("N cores must be > 0");
                break;
            case 3:
                if ( (value instanceof String) ) description = (String) value;
                else warning("Bad desciption: object has class " +
                       ((value == null)?"null":value.getClass().getName()));
                break;
            default:
        }
    }
    
    public boolean isFullySpecified() {
        return( (instanceType != null) && // (name != null) && (name.length() > 0) &&
                (vt != null) &&
                (nCores > 0)
               );
    }

    private VirtualizationType getVT(Object value) {
        try {
            if ( !(value instanceof String) ) return(null);        
            return( VirtualizationType.valueOf( ((String) value).trim() ) );
        }
        catch(Exception xxx) { return(null); }
    }
    
    private int getNC(Object val) {
        try {
            Integer iv;
            if ( val instanceof Integer ) iv = (Integer) val;
            else if ( val instanceof String ) iv = Integer.parseInt((String) val);
            else return(0);
            return( ((Integer) iv).intValue() );
        }
        catch(Exception x) { return(0); }
    }
    
    private void warning(String message) {
        GuiUtil.warning(new String[] { message }, "Entry error");
    }      
    
    
        // ---------- Statics -------------------------------------
    
    /** Retrieves the instance types from s3.
     * 
     * @return
     * @throws Exception 
     */
    public static List<Ec2InstanceType> getInstanceTypes() throws Exception {
        
        S3Access s3a = new S3Access();
        
        return((List<Ec2InstanceType>) s3a.getObject(
                                        ConstantsUM.PPE_INFO_BUCKET_NAME,
                                        ConstantsUM.INSTANCE_TYPE_LIST_KEY));
    }
}
