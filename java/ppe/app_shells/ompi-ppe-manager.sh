#    ----------------------------------------------------------------------------
#
#    Copyright 2011 Northbranchlogic, Inc.
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
#    ----------------------------------------------------------------------------

# Launch ppe.ompi.OmpiPPEManager

# Save the cd
UCD=`pwd`

# Get the dir where this script resides.
dot=`dirname "$0"`/.

# Set the bin and lib dirs
B=`cd "$dot" && pwd`
cd $B; cd ..
H=`pwd`
LIB=$H/lib

# Set the class path
CP=$LIB/ppe.jar:$LIB/aws-java-sdk-1.3.3.jar:\
$LIB/commons-codec-1.3.jar:\
$LIB/httpclient-4.1.1.jar:\
$LIB/httpcore-4.1.jar:\
$LIB/commons-logging-1.1.1.jar:\
$LIB/ganymed-ssh2-build251beta1.jar:\
$LIB/trove.jar:\
$LIB/utilj.jar

# cd to the users's initial dir
cd $UCD

# Now we can run the app.

java -classpath $CP ppe.ompi.OmpiPPEManager

