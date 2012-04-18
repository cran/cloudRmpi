#
#    Copyright 201# Northbranchlogic, Inc.
#
#    This file is part of Parallel Processing with EC2 (ppe).
#
#    ppe is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    ppe is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
#
# --------------------------------------------------------------------------
#
# user_monitor.sh
#
# This script is used for launching the user monitor (server) app,
# norbl.com.cbp.ppe.usermonitor.UserMonitor
#
# This script must be run on an ec2 instance and must run as root.
#
# --------------------------------------------------------------------------

cd /root

java -classpath "lib/*" com.norbl.cbp.ppe.usermonitor.UserMonitor configFile=user_server_config