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

package ppe;

import ppe.ec2.*;
import ppe.ompi.*;
import java.util.*;
import java.lang.reflect.*;

/** <tt>Services</tt> specifies operations to performed on a network
 *  after its ec2 instances are running, such as configuring Open MPI.
 *  The <tt>Services</tt> object is used to determine the state of
 *  the services - this is used for building the state field in the
 *  gui network table.<p>
 *
 *  Note that a <tt>Services</tt> object exists for each network,
 *  whether or not the service is running.<p>
 *
 *  Every {@link NetworkInfo} object has a <tt>Services</tt> object.<p>
 *
 *  This class maintains a static hash table of <tt>Services</tt> objects,
 *  keyed by network ID.
 *
 *
 * @author Barnet Wagman
 */
abstract public class Services {

    private static Class servicesClass;

    protected String networkID;
    
    protected Ec2Wrangler ec2w;
    protected NetworkSpec networkSpec;
    protected OmpiSpec ompiSpec;

    public Services(String networkID) {
        this.networkID = networkID;
    }

    abstract public void launch();
    abstract public String getStateTitle();
    abstract public boolean isRunning();
    abstract public boolean isPending();
    abstract public boolean notRunning();
    abstract public boolean inNilState();
    
    /** Sets objects needed to start services.
     * 
     * @param ec2w
     * @param networkSpec
     * @param ompiConfig 
     */
    public void set(Ec2Wrangler ec2w, NetworkSpec networkSpec,
                    OmpiSpec ompiSpec) {
        this.ec2w = ec2w;
        this.networkSpec = networkSpec;
        this.ompiSpec = ompiSpec;
    }

    public static void setServicesClass(Class servicesClass) {
        Services.servicesClass = servicesClass;
    }

        /** Creates the service object for <tt>networkID</tt> and stores it
         *  in the hash table (keyed by <tt>networkID</tt>.
         * @param networkID
         */
    public static Services createServices(String networkID) {
        try {
           Constructor cons =
                servicesClass.getConstructor(new Class[] {String.class} );
           Services s = (Services) cons.newInstance(new Object[] {networkID} );
           return(s);
        }
        catch(Exception xxx) {
            if ( xxx.getCause() != null ) 
                throw new RuntimeException(xxx.getCause());            
            else throw new RuntimeException(xxx);
        }
    }
}
