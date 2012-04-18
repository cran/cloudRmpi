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

import java.text.*;
import java.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class TimeUtil {

    public static int toYMD(long tm) {
        return(Integer.parseInt(
                    (new SimpleDateFormat("yyyyMMdd")).format(
                    new Date(tm)))
               );
    }

    public static long toYMDHMSM(long tm) {
        return(Long.parseLong(
                    (new SimpleDateFormat("yyyyMMddHHmmssSSS")).format(
                    new Date(tm)))
               );
    }

    public static String toDateTimeString(long tm) {
        return((new SimpleDateFormat("yyyy MM dd HH mm ss")).format(
                    new Date(tm))
               );
    }

}
