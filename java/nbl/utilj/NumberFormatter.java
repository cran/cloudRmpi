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

package nbl.utilj;

import java.text.*;
import java.math.*;

/**
 *
 * @author Barnet Wagman
 */
public class NumberFormatter {
    public static NumberFormat d1Formatter;
    public static NumberFormat d2Formatter;
    public static NumberFormat d4Formatter;
    public static NumberFormat d5Formatter;
    public static NumberFormat d6Formatter;

    public static BigDecimal ZERO_BIG_DECIMAL =
        new BigDecimal("0.00");

    static {
        d1Formatter = NumberFormat.getInstance();
        d1Formatter.setMaximumFractionDigits(1);
        d1Formatter.setMinimumFractionDigits(1);
        d1Formatter.setGroupingUsed(false);
        
        d2Formatter = NumberFormat.getInstance();
        d2Formatter.setMaximumFractionDigits(2);
        d2Formatter.setMinimumFractionDigits(2);
        d2Formatter.setGroupingUsed(false);

        d4Formatter = NumberFormat.getInstance();
        d4Formatter.setMaximumFractionDigits(4);
        d4Formatter.setMinimumFractionDigits(4);
        d4Formatter.setGroupingUsed(false);

        d5Formatter = NumberFormat.getInstance();
        d5Formatter.setMaximumFractionDigits(5);
        d5Formatter.setMinimumFractionDigits(5);
        d5Formatter.setGroupingUsed(false);

        d6Formatter = NumberFormat.getInstance();
        d6Formatter.setMaximumFractionDigits(6);
        d6Formatter.setMinimumFractionDigits(6);
        d6Formatter.setGroupingUsed(false);
    }

    public static String f2(Object x) {
        if ( x instanceof BigDecimal )
             return(d2Formatter.format( ((BigDecimal) x).doubleValue() ));
        if ( x instanceof Double )
            return(d2Formatter.format( ((Double) x).doubleValue() ));
        else if ( x instanceof Long )
            return(d2Formatter.format( ((Long) x).longValue() ));
        else if ( x instanceof Float )
            return(d2Formatter.format( ((Float) x).floatValue() ));
        else if ( x instanceof Integer )
            return(d2Formatter.format( ((Integer) x).intValue() ));

        return( x.getClass().getName() + " " + x.toString() );
    }

    public static String f4(Object x) {
        if ( x instanceof Double )
            return(d4Formatter.format( ((Double) x).doubleValue() ));
        else if ( x instanceof Long )
            return(d4Formatter.format( ((Long) x).longValue() ));
        else if ( x instanceof Float )
            return(d4Formatter.format( ((Float) x).floatValue() ));
        else if ( x instanceof Integer )
            return(d4Formatter.format( ((Integer) x).intValue() ));

        return( x.toString() );
    }

    public static String f5(Object x) {
        if ( x instanceof Double )
            return(d5Formatter.format( ((Double) x).doubleValue() ));
        else if ( x instanceof Long )
            return(d5Formatter.format( ((Long) x).longValue() ));
        else if ( x instanceof Float )
            return(d5Formatter.format( ((Float) x).floatValue() ));
        else if ( x instanceof Integer )
            return(d5Formatter.format( ((Integer) x).intValue() ));

        return( x.toString() );
    }

    public static String f6(Object x) {
        if ( x instanceof Double )
            return(d6Formatter.format( ((Double) x).doubleValue() ));
        else if ( x instanceof Long )
            return(d6Formatter.format( ((Long) x).longValue() ));
        else if ( x instanceof Float )
            return(d6Formatter.format( ((Float) x).floatValue() ));
        else if ( x instanceof Integer )
            return(d6Formatter.format( ((Integer) x).intValue() ));

        return( x.toString() );
    }

    public static String f2(double x) {
        return(d2Formatter.format(x));
    }

    public static String f1(double x) {
        return(d1Formatter.format(x));
    }
}
