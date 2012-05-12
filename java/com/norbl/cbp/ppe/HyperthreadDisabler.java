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
import com.norbl.util.ssh.*;
import java.util.*;
import java.io.*;

/** Disables hyperthreading using
 *  <blockquote><tt>
 *  echo 0 &lt; /sys/devices/system/node/node0/cpu2/online
 *  </tt></blockquote> via ssh.
 *
 * @author Barnet Wagman
 * @deprecated 
 */
public class HyperthreadDisabler {

//    Log log;

    public HyperthreadDisabler() {
//        log =  LogFactory.getLog(NearNilLog.class);
    }    

        /** This method attempts to reduce the number of cpu 'online'
         *  to nCores.
         * @param ssh
         * @param nCores
         */
    public boolean disable(SshExec ssh, int nCores) {

        try {
                // Get a list of the online cpus and online cpus
            List<Cpu> cpus = findCpus(ssh);
            PPEApp.verbose("# HyperthreadDisabler # found n cpus=" + cpus.size() + 
                           " nCores=" + nCores);
            
            if ( cpus.size() <= nCores ) { // Probably no hyperthreading              
                return(false);
            }
            
            List<Cpu> online = getOnlineCpus(cpus);
            PPEApp.verbose("# HyperthreadDisabler # N cpu online=" + 
                            online.size());

            int nAccessible = Math.min(cpus.size(),online.size());
            
            int nToDisable = nAccessible - nCores; // cpus.size() - nCores;  
            
            PPEApp.verbose("# HyperthreadDisabler # N to disable=" + nToDisable);
            
            if ( nToDisable < 1 ) return(false);
            
            if ( nToDisable >= online.size() ) {
                PPEApp.verbose("n to disable=" + nToDisable + " but " +
                        " n online=" + online.size() + " soo no disabling.");
                return(false);
            }
                      
            Collections.sort(online);

                // We want to disable to highest numbers, so work
                // through online backwards.
            int idx = online.size() - 1;
            for ( int i = 0; i < nToDisable; i++ ) {
                disableCpu(ssh,online.get(idx));                
                --idx;
            }
            return(true);
        }
        catch(Exception iox) {
                // We do not want an exception here to kill the app, so
                // we'll just print the message.
            ExceptionHandler.display(iox);
            return(false);
        }
    }

    List<Cpu> findCpus(SshExec ssh) throws IOException {

        String ns = ssh.execRead("sudo ls -1d " +
            "/sys/devices/system/node/node0/cpu[0123456789]*",
            1000L * 10L);       
        String[] nodeLines = ns.trim().split("\n");

        List<Cpu> cpus = new ArrayList<Cpu>();
        for ( String s : nodeLines) {
            if ( s.startsWith("/sys/devices"))
                cpus.add(new Cpu(s,ssh));
        }
        return(cpus);
    }
    
    

    List<Cpu> getOnlineCpus(List<Cpu> cpus) {
        List<Cpu> online = new ArrayList<Cpu>();
        for ( Cpu c : cpus ) {
            if ( c.isOnline ) online.add(c);            
        }
        return(online);
    }

    void disableCpu(SshExec ssh, Cpu cpu) throws IOException {
        ssh.exec("sudo echo 0 > " + cpu.pathToOnline,1000L * 10L);
        // SUDO FAILS: need to be su
    }

        // --------------------------------------

    class Cpu implements Comparable {
        String pathToCpuNumber;
        String pathToOnline;
        int cpuNumber;
        boolean isOnline;

        public Cpu(String line,SshExec ssh) throws IOException {
            pathToCpuNumber = line;
            String[] flds = line.split("/");
            String cn = flds[flds.length-1].trim();
            String ns = cn.substring(3);
            cpuNumber = Integer.parseInt(ns);

            String ons = line + "/online";
            String isOn = ssh.execRead("sudo " +
                "[ -f " + ons + "] && echo 'YES' || echo 'NO'",
                 1000L * 10L);
            if ( isOn.equals("YES") ) {
                isOnline = true;
                pathToOnline = ons;
            }
            else isOnline = false;
        }

        public int compareTo(Object other) {
            Cpu o = (Cpu) other;
            if ( this.cpuNumber > o.cpuNumber ) return(1);
            else if ( this.cpuNumber < o.cpuNumber ) return(-1);
            else return(0);
        }

        public String toString() {
            return(pathToOnline + " " + cpuNumber + " " + isOnline);
        }
    }
}
