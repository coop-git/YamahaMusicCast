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

public class UdpMessage {

    @SerializedName("device_id")
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    @SerializedName("main")
    private Main main;
    @SerializedName("zone2")
    private Zone2 zone2;
    @SerializedName("zone3")
    private Zone3 zone3;
    @SerializedName("zone4")
    private Zone4 zone4;


    public Main getMain() {
        return main;
    }
    public Zone2 getZone2() {
        return zone2;
    }
    public Zone3 getZone3() {
        return zone3;
    }
    public Zone4 getZone4() {
        return zone4;
    }



    public class Main {
        @SerializedName("power")
        private String power;

        public String getPower() {
            return power;
        }

    }

    public class Zone2 {
        @SerializedName("power")
        private String power;

        public String getPower() {
            return power;
        }

    }

    public class Zone3 {
        @SerializedName("power")
        private String power;

        public String getPower() {
            return power;
        }

    }

    public class Zone4 {
        @SerializedName("power")
        private String power;

        public String getPower() {
            return power;
        }

    }








}


