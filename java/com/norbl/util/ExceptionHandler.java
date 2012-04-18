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

package com.norbl.util;

import com.norbl.util.gui.*;

/** Primarily used for debugging.
 *
 * @author Barnet Wagman
 */
public class ExceptionHandler {

    public static boolean useGui = true;

    public static void display(Throwable x) {
        if ( useGui ) gui(x);
        else text(x);
    }

    public static void text(Throwable x) {
        Throwable c = x.getCause();
        if ( (c != null) && (c.getMessage() != null) &&
              (c.getStackTrace() != null) ) {
            text(c);
        }
        else {
            StringBuilder s = new StringBuilder("* ExceptionHandler:\n");
            String mess = x.getMessage();
            String stack = stackTraceToString(x.getStackTrace());
            if ( mess != null ) s.append(mess + "\n");
            else s.append("null exception message\n");
            if ( stack != null ) s.append(stack + "\n");
            System.err.println(s.toString());
        }
    }

    public static void gui(Throwable x) {
        Throwable c = x.getCause();
        if ( (c != null) && (c.getMessage() != null) &&
              (c.getStackTrace() != null) ) {
            gui(c);
        }
        else GuiUtil.exceptionMessage(x);
    }

    public static String stackTraceToString(StackTraceElement[] trace) {
        if ( trace == null ) return("NULL stack trace.");
        String s = "";
        for ( int i = 0; i < trace.length; i++ ) {
            if ( trace[i] != null )
                s += trace[i].toString() + "\n";
            else s += "null trace element at [" + i + "]";
        }
        return(s);
    }

    public static RuntimeException toRuntimeException(Throwable x) {

        Throwable cause = x.getCause();
        if ( (cause != null) && (cause.getStackTrace() != null) &&
             (cause.getStackTrace().length > 0) ) {
            if ( cause instanceof RuntimeException )
                return((RuntimeException) cause);
            else {
                RuntimeException rx = new RuntimeException(cause.getMessage());
                rx.setStackTrace(cause.getStackTrace());
                return(rx);
            }
        }
        else {
            RuntimeException rx = new RuntimeException(x.getMessage());
            rx.setStackTrace(x.getStackTrace());
            return(rx);
        }
    }
}
