#    ----------------------------------------------------------------------------
#
#    Copyright 2012 Northbranchlogic, Inc.
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

#    Shell script to launch the Open MPI PPE network manager,
#    com.norbl.cbp.ppe.ompi.OmpiPPEApp
#
#    This script requires that (i) a Java interpreter (>= 1.6) is in the
#    PATH and (ii) that this script is in a directory that contains 
#    a subdirectory ('jars') of all jars used by the app.
#
#   ----------------------------------------------------------------------------

# Save the cd
UCD=`pwd`

# Get the dir where this script resides.
dot=`dirname "$0"`/.

# Set the bin and lib dirs
B=`cd "$dot" && pwd`
cd $B; cd ..
H=`pwd`
LIB=$H/lib

# cd to the users's initial dir
cd $UCD

# Now we can run the app.

java -classpath "$LIB/*" com.norbl.cbp.ppe.ompi.OmpiPPEApp
