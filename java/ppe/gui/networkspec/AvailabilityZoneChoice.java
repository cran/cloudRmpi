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

package ppe.gui.networkspec;

import com.amazonaws.services.ec2.model.*;

/**
 *
 * @author Barnet Wagman
 */
public class AvailabilityZoneChoice extends Choice {

    public static final String ANY_ZONE = "Use any zone.";

    AvailabilityZone zone;

        /**
         *
         * @param zone if null, the zone is not specified.
         */
    public AvailabilityZoneChoice(AvailabilityZone zone) {
        super();
        this.zone = zone;
    }

    public String getChoiceType() {
        return(this.getClass().getSimpleName());
    }

    public Object getValue() { return(zone); }

    public String getLabel() {
        if ( zone != null ) return(zone.getZoneName());
        else return(ANY_ZONE);
    }

    public void createCC() {
        cc = new RadioButtonCC(getLabel());
        cc.setEnabled(enabled);
        cc.setSelected(selected);
    }
}
