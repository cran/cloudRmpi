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

/**
 *
 * @author Barnet Wagman
 */
public class ConstantsPPE {

    public static long SERIAL_VERSION_UID = 0L;

    public static String CONFIG_FILE_NAME = ".ppe-config";
    
    public static String TMP_DIR = ".ppe-tmp";

    public static final long ONE_HOUR_MILLIS = 1000L * 60L * 60L;

    public static final long PING_NETWORK_MAX_MILLIS = 1000L * 60L * 10L;
    
    public static final String OMPI_AMI_GROUP = "ppe-ompi-ami";
    
    public static final String CLOUDRMPI_AMI_GROUP = "cloudrmpi-ami";
}
