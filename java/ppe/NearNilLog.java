/*
    Copyright 2011 Barnet Wagman

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

/** An implementation of <tt>org.apache.commons.logging.Log</tt>
 *  that suppresses most log messages and writes the other to the stdout.<p>
 *
 *  Usage: add the line
 *  <blockquote><tt>
 *      System.setProperty("org.apache.commons.logging.Log",
 *                         "mpiec2.NearNilLog");
 *  </tt></blockquote> at the beginning of your application.
 *
 * @author Barnet Wagman
 */
public class NearNilLog implements org.apache.commons.logging.Log {
    
    public NearNilLog(String x) {}

    public void debug(Object arg0) {
        
    }

    public void debug(Object arg0, Throwable arg1) {
        
    }

    public void error(Object arg0) {
//        System.out.println(arg0);
    }

    public void fatal(Object arg0) {
//        System.out.println(arg0);
    }

    public void error(Object arg0, Throwable arg1) {
//        System.out.println(arg0 + "\n" + arg1.getMessage());
    }

    public void fatal(Object arg0, Throwable arg1) {
//        System.out.println(arg0 + "\n" + arg1.getMessage());
    }

    public void info(Object arg0, Throwable arg1) {
        
    }

    public void trace(Object arg0) {
        
    }

    public void info(Object arg0) {
        
    }

    public boolean isDebugEnabled() {
        return(false);
    }

    public boolean isErrorEnabled() {
        return(true);
    }

    public boolean isFatalEnabled() {
        return(true);
    }

    public boolean isInfoEnabled() {
        return(false);
    }

    public boolean isTraceEnabled() {
        return(false);
    }

    public boolean isWarnEnabled() {
        return(false);
    }

    public void warn(Object arg0) {
        
    }

    public void trace(Object arg0, Throwable arg1) {
        
    }

    public void warn(Object arg0, Throwable arg1) {
        
    }
    
    

}
