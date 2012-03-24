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

package ppe.ompi;

import ppe.*;
import ppe.ec2.*;
import ch.ethz.ssh2.*;
import utilssh.*;

/**
 *
 * @author Barnet Wagman
 */
public class OmpiConfigServices extends Services {

    private enum State { nil, pending, running, notRunning };

    private static final long TEST_INTERVAL = 1000L * 15L;
    private static final long MAX_TEST_AGE = 1000L * 60L * 5L;

    private State state;

    OmpiConfig oc;

    private long initialTestTime, lastTestTime;

    public OmpiConfigServices(String networkID) {
        super(networkID);
        setState(State.nil);
        oc = null;
        initialTestTime = -1L;
        lastTestTime = -1L;
    }

    private synchronized void setState(State state) {
        this.state = state;
        NiM.fireStateChangeEvent();
    }

    public boolean isRunning() {
        findState();
        return( State.running.equals(state) );
    }

    public boolean isPending() {
        findState();
        return( State.pending.equals(state) );
    }

    public boolean notRunning() {
        findState();
        return( State.notRunning.equals(state) );
    }

    public boolean inNilState() {
        findState();
        return( State.nil.equals(state) );
    }

    private void findState() {
        if ( !State.nil.equals(state) ) return;
            // The state has been set, no check required.
        else if ( oc == null ) return;
            // This should NOT happen.  If oc exists, it should
            // have set the state.
        else { // Determine the state by querying the master if possible. 
               // This is done in a thread to make sure we don't interfere
               // with the awt event thread.
           
            (new Thread(new Runnable() { public void run() {
                checkConfiguration();
                }
            })).start();
        }
    }
//
//        /////////
//        if ( State.running.equals(state) ||
//             State.pending.equals(state) ||
//             State.notRunning.equals(state) ) return;
//        else if ( oc != null ) {
//            if ( oc.isConfigured() ) {
//                setState(State.running);
//            }
//            else if ( oc.isPending() ) {
//                setState(State.pending);
//            }
//            else { // Bad case, but ...
//                if ( isConfigured() ) setState(State.running);
//                else setState(State.notRunning);
//            }
//        }
//        else { // No oc object - either this instance of the app
//               // didn't launch the network or instances aren't
//               // up yet.
//            NetworkInfo ni = NiM.getForID(networkID);
//            if (ni.isPending() || ni.isNil() ) { // Too early to say
//                setState(State.nil);
//            }
//            else if ( ni.isRunning() ) { // Check the master.
//
//                if ( pastMaxTestTime() ) {
//                    setState(State.notRunning);
//                }
//                else if ( timeForTest() ) {
//                    if ( isConfigured() ) setState(State.running);
//                    else  setState(State.nil);
//                    lastTestTime = System.currentTimeMillis();
//                }
//            }
//            else setState(State.nil);
//        }
//    }
//
//    private boolean pastMaxTestTime() {
//        if ( initialTestTime < 0L ) {
//            initialTestTime = System.currentTimeMillis();
//            return(false);
//        }
//        else return( System.currentTimeMillis()
//                     >
//                     (initialTestTime + MAX_TEST_AGE) );
//    }
//
//    private boolean timeForTest() {
//        return( (lastTestTime < 0L) ||
//                ((lastTestTime + TEST_INTERVAL)
//                 >
//                 System.currentTimeMillis())
//              );
//    }

    public String getStateTitle() {

        findState();

        switch(state) {
            case nil: return("determining ompi state");
            case running: return("ompi configured");
            case notRunning: return("ompi not configured");
            case pending:
                if ( oc != null ) {
                    return( oc.getStateDescription() );
                }
                else throw new RuntimeException("OmpiConfigServices state == " +
                            "pending but OmpiConfi == null");
            default: return("-");
        }
    }

//    public void configOmpi(Ec2Wrangler ec2w,
//                           NetworkSpec networkSpec, OmpiSpec ompiSpec) {
//
//        setState(State.pending);
//        oc = new OmpiConfig(ec2w, ompiSpec);
//        if ( oc.config(networkID) ) setState(State.running);
//        else setState(State.notRunning);
//    }
    
    public void launch() {

        setState(State.pending);
        oc = new OmpiConfig(ec2w, ompiSpec);
        if ( oc.config(networkID) ) setState(State.running);
        else setState(State.notRunning);
    }

        /** Checks the configuration by looking at the master instance.
         *  Resets the state if appropriate.
         *
         */
    void checkConfiguration() {
        Connection con = null;
        try {
            NetworkInfo ni = NiM.getForID(networkID);
            if ( ni == null ) return;
      
            con = Ssh.connect(ni.getMastersPublicDnsName(),
                              ppe.ec2.Constants.EC2_USERNAME,
                              ppe.gui.PPEManager.paramsEc2.rsaKeyPairFile,// spec.rsaKeyPairFile,
                              utilssh.Constants.SSH_CONNECTION_TIMEOUT_MILLIS);
            SshExec sx = new SshExec(con);
            boolean boo =
                    sx.fileExists(ppe.ec2.Constants.EC2_USER_HOME_DIR + "/" +
                                  Constants.OMPI_HOSTFILE_NAME)
                    &&
                    sx.fileExists(ppe.ec2.Constants.EC2_USER_SSH_DIR + "/" +
                                  Constants.PPE_MASTER_KEY_PAIR_FILENAME)
                  ;
            if ( boo ) setState(State.running);
            else setState(State.notRunning);
        }
        catch(Exception xxx) {
            // ExceptionHandler.text(xxx);
        }
        finally {
            if ( con != null ) con.close();
        }
    }
}
