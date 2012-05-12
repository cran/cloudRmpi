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

package com.norbl.cbp.ppe.ompi;

import com.norbl.cbp.ppe.*;
import ch.ethz.ssh2.*;
import com.norbl.util.ssh.*;

/**
 *
 * @author Barnet Wagman
 */
public class OmpiConfigServices extends Services {

    private enum State { nil, pending, running, notRunning };

    private static final long TEST_INTERVAL = 1000L * 15L;
    private static final long MAX_TEST_AGE = 1000L * 60L * 5L;

    private State state;

    OmpiConfigurer oc;

    private long initialTestTime, lastTestTime;
    
    private Ec2Wrangler ec2w;
    private NetworkSpec networkSpec;
    private ParamsEc2 paramsEc2;

    public OmpiConfigServices(String networkID) {
        super(networkID);
        setState(State.nil);
        oc = null;
        initialTestTime = -1L;
        lastTestTime = -1L;
    }
    
    public void set(Ec2Wrangler ec2w) { this.ec2w = ec2w; }
    
    public void set(NetworkSpec networkSpec) { 
        this.networkSpec = networkSpec;
    }
    public void set(ParamsEc2 paramsEc2) { this.paramsEc2 = paramsEc2; }

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
    
    public void launch() {

        setState(State.pending);
        oc = new OmpiConfigurer(ec2w,networkSpec,paramsEc2);
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
                              ConstantsEc2.EC2_USERNAME,
                              PPEManager.paramsEc2.rsaKeyPairFile,// spec.rsaKeyPairFile,                             
                              ConstantsSSH.SSH_CONNECTION_TIMEOUT_MILLIS);
            SshExec sx = new SshExec(con);
            boolean boo =
                    sx.fileExists(ConstantsEc2.EC2_USER_HOME_DIR + "/" +
                                  ConstantsOmpi.OMPI_HOSTFILE_NAME,
                                  1000L * 10L)
                    &&
                    sx.fileExists(ConstantsEc2.EC2_USER_SSH_DIR + "/" +
                                  ConstantsOmpi.PPE_MASTER_KEY_PAIR_FILENAME,
                                  1000L * 10L
                                  )
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
    
    public NetworkSpec getNetworkSpec() {
        return(networkSpec);
    }
}
