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

package ppe.ec2;

/**
 *
 * @author Barnet Wagman
 */
public class Constants {

    public static String CLUSTER_GROUP_DEFAULT = "ppe-cluster";
    public static String SECURITY_GROUP_DES = "Security group for ppe.";
    public static String NETWORD_ID_PREFIX = "ppe-";
    public static Long SPOT_STATE_NAP_TIME = 2000L;

        /** ec2 images all have a user with this name.
         */
    public static String EC2_USERNAME = "ec2-user";
    public static String EC2_USER_HOME_DIR = "/home/" + EC2_USERNAME;
    public static String EC2_USER_SSH_DIR = EC2_USER_HOME_DIR + "/.ssh";

    public static String INSTANCE_TYPE_FILENAME = "ec2-instance-types";
    public static String AMI_ID_FILENAME = "mpi-ec2-ami-IDs";
    public static String CONFIG_DIR_NAME = "config";
    public static String DEFAULT = "default";
    public static long NETWORK_INFO_UPDATE_INTERVAL = 1000L * 5L;

        /** This is now a LONG wait: 15 minutes.  It determines
         *  the max wait for a network to become visible via
         *  aws op.
         */
    public static long MAX_WAIT_FOR_NEW_NETWORK_INFO =
                                1000L * 60L * 15L;
                                // 1000L * 16L;
}
