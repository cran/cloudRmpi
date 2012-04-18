/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of utilssh.

    utilssh is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    utilssh is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.norbl.util.ssh;


/** Thrown if a <i>required</i> parameter has not been specified in the
 *  config file or argv.
 *
 * @author Barnet Wagman
 */
public class ConnectFailureException extends Exception {

    public ConnectFailureException(String message) {
        super(message);
    }
}
