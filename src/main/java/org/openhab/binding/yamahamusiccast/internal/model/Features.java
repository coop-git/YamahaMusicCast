/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yamahamusiccast.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the push request sent to the API.
 *
 * @author Lennert Coopman - Initial contribution
 */

public class Features {

    @SerializedName("response_code")
    private String responseCode;

    public String getResponseCode() {
        return responseCode;
    }

    @SerializedName("system")
    private System system;

    public System getSystem() {
        return system;
    }

    public class System {
        @SerializedName("zone_num")
        private String zoneNum;

        public String getZoneNum() {
            return zoneNum;
        }
    }
}
