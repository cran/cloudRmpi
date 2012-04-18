/*
    Copyright 2011 Northbranchlogic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.norbl.util;

/** I'm sick of typing these strings.
 *
 * @author Barnet Wagman
 */
public enum SysProp {
    java_version, //  Java Runtime Environment version
    java_vendor, // Java Runtime Environment vendor
    java_vendor_url, // Java vendor URL
    java_home, // Java installation directory
    java_vm_specification_version, // Java Virtual Machine specification version
    java_vm_specification_vendor, // Java Virtual Machine specification vendor
    java_vm_specification_name, // Java Virtual Machine specification name
    java_vm_version, // Java Virtual Machine implementation version
    java_vm_vendor, // Java Virtual Machine implementation vendor
    java_vm_name, // Java Virtual Machine implementation name
    java_specification_version, // Java Runtime Environment specification version
    java_specification_vendor, // Java Runtime Environment specification vendor
    java_specification_name, // Java Runtime Environment specification name
    java_class_version, // Java class format version number
    java_class_path, // Java class path
    java_library_path, // List of paths to search when loading libraries
    java_io_tmpdir, // Default temp file path
    java_compiler, // Name of JIT compiler to use
    java_ext_dirs, // Path of extension directory or directories
    os_name, // Operating system name
    os_arch, // Operating system architecture
    os_version, // Operating system version
    file_separator, // File separator ("/" on UNIX)
    path_separator, // Path separator (":" on UNIX)
    line_separator, // Line separator ("\n" on UNIX)
    user_name, // User's account name
    user_home, // User's home directory
    user_dir  // User's current working directory
    ;

    public String getKey() {
        return( this.toString().replace('_','.') );
    }

    public String getVal() {
        return( System.getProperty(getKey()) );
    }

    public static void main(String[] argv) {
        System.out.println("user home=" + SysProp.user_home.getVal() + "\n" +
                "user dir=" + SysProp.user_dir.getVal() + "\n" +
                "java class path=" + SysProp.java_class_path.getVal() + "\n" +
                "java ext dir=" + SysProp.java_ext_dirs.getVal() );
    }
}
