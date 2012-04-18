/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.norbl.util.*;
import com.norbl.util.gui.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
public class NetworkPinger {

    enum PingFailedResponse {
        wait("Continue to wait"),
        proceed("Proceed"),
        terminate("Terminate and proceed");

        String title;
        PingFailedResponse(String title) {
            this.title = title;
        }
    }
    
    Ec2Wrangler ec2w;
    
    public NetworkPinger(Ec2Wrangler ec2w) {
        this.ec2w = ec2w;
    }
    
        /**
         *
         * @param networkID
         * @return false if there are NO usable instances.
         */
    protected boolean pingNetwork(String networkID) {

        for (;;) {
            try {
                Pinger pinger = Pinger.pingNetwork(networkID,
                                ConstantsPPE.PING_NETWORK_MAX_MILLIS);
                if ( pinger.allSucceeded() ) return(true);
                else {
                    PingFailedResponse r = getPingFailedResponse(pinger);
                    if ( PingFailedResponse.proceed.equals(r) ) {
                        return( pingFailedRetag(pinger) );
                    }
                    else if ( PingFailedResponse.terminate.equals(r) ) {
                        return( terminatePingFailures(pinger) );
                    }
                    // else continue waiting
                }
            }
            catch(NoSuchNetworkException nx) {
                ExceptionHandler.gui(nx);
                    GuiUtil.warning(new String[] {
                        "   Ompi may not have been configured"
                        },
                        "Warning");
            }
        }
    }

    PingFailedResponse getPingFailedResponse(Pinger pinger) {

        Object[] options = { PingFailedResponse.wait.title,
                             PingFailedResponse.terminate.title,
                             PingFailedResponse.proceed.title                             
                           };

        int rn = JOptionPane.showOptionDialog(null,
                                              buildPingFailedMessage(pinger),
                                              "Unresponsive instance",
                                              JOptionPane.YES_NO_CANCEL_OPTION,
                                              JOptionPane.WARNING_MESSAGE,
                                              null,
                                              options,
                                              options[0]);


        String r = (String) options[rn];
        if ( PingFailedResponse.wait.title.equals((r)) )
            return(PingFailedResponse.wait);
        else if (PingFailedResponse.proceed.title.equals((r)))
            return(PingFailedResponse.proceed);
        else if (PingFailedResponse.terminate.title.equals((r)))
            return(PingFailedResponse.terminate);
        else throw new RuntimeException("Undefined reponse=" + rn + " " + r);

    }
    
    String buildPingFailedMessage(Pinger pinger) {

        StringBuilder s = new StringBuilder("<html>");

        s.append("The following instances are <i>not</i> responding: " +
                  "<blockquote>");
        s.append( pinger.failureNamesToHtmlLines() );
        s.append("</blockquote>");
        s.append("<br>You have three choices.<ul>");
        s.append("<li>Continue to wait for the instances to respond. " +
                 " if there's no response for 10 minutes, you'll be " +
                 " given this choice again.</li>");
        s.append("<li>Terminate the unresponsive instances and proceed.</li>");
        s.append("<li>Proceed but do not terminate the unresponsive " +
                  " instances. They will be " +
                   "omitted from the network but they will continue to " +
                   "run.</li>");
        s.append("</ul><br>");
        s.append("</html>");

        return(s.toString());
    }
    
       /** Instances that failed to respond to ssh ping are
         *  retagged as non network members. If one of these is the
         *  master, a new master is created.
         * @param pinger
         * @return false if there are NO usable instances
         */
    private boolean pingFailedRetag(Pinger pinger) {

        if ( (pinger.failures == null) || (pinger.failures.size() < 1) ) {
            if ( pinger.successes != null )
                return( pinger.successes.size() > 0 );
            else return(false);
        }

        boolean masterFailed = false;

        for ( InstanceStatus f : pinger.failures ) {
            if ( f.isMaster() ) masterFailed = true;
            String ID = f.instance.getInstanceId();
            ec2w.setTags(ID,"nil","nil - unresponsive instance",
                         NodeType.slave);
        }

        if ( masterFailed ) { // Retag one responsive slave to master.

            if ( (pinger.successes == null) || (pinger.successes.size() < 1) ) {
                ExceptionHandler.display(new RuntimeException("There are " +
                        " no usable instances."));
                return(false);
            }

            InstanceStatus m = pinger.successes.get(0);
            ec2w.setTags(m.instance.getInstanceId(),
                         m.getNetworkID(),
                         m.getNetworkName(),
                         NodeType.master);
        }

        return(true);
    }

        /** Terminates instances that did not respond to ping.
         *  Before terminating, calls {@link #pingFailedRetag(ppe.util.Pinger)}
         *  to ensure that we have a master.
         *
         * @param pinger
         * @return false if there are no usable instancers.
         * 
         */
    private boolean terminatePingFailures(Pinger pinger) {

        pingFailedRetag(pinger);

        if ( (pinger.failures == null) || (pinger.failures.size() < 1) ) {
            if ( pinger.successes != null )
                return(pinger.successes.size() > 0 );
            else return(false);
        }

        List<String> failedIDs = new ArrayList<String>();
        for ( InstanceStatus f : pinger.failures ) {
            failedIDs.add(f.instance.getInstanceId());            
        }

        ec2w.terminateInstances(failedIDs);

        GuiUtil.warning(buildTermMessage(pinger.failures),
                                         "Terminated instances");

        return(true);
    }
    
    private String[] buildTermMessage(List<InstanceStatus> failures) {

        List<String> lines = new ArrayList<String>();
        lines.add("The following instances were terminated:");
        lines.add(" ");

        for ( InstanceStatus f : failures ) {
            lines.add("    " + f.getPublicDnsName());
        }

        return( lines.toArray(new String[ lines.size() ]) );
    }
}
