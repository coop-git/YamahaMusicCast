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
    private Zone main;
    @SerializedName("zone2")
    private Zone zone2;
    @SerializedName("zone3")
    private Zone zone3;
    @SerializedName("zone4")
    private Zone zone4;
    @SerializedName("netusb")
    private NetUSB netusb;

    public Zone getMain() {
        return main;
    }

    public Zone getZone2() {
        return zone2;
    }

    public Zone getZone3() {
        return zone3;
    }

    public Zone getZone4() {
        return zone4;
    }

    public NetUSB getNetUSB() {
        return netusb;
    }

    public class Zone {
        @SerializedName("power")
        private String power;
        @SerializedName("volume")
        private Integer volume = Integer.valueOf(0);
        @SerializedName("mute")
        private String mute;
        @SerializedName("input")
        private String input;
        @SerializedName("status_updated")
        private String statusUpdated;

        public String getPower() {
            if (power == null) {
                power = "";
            }
            return power;
        }

        public String getMute() {
            if (mute == null) {
                mute = "";
            }
            return mute;
        }

        public String getInput() {
            if (input == null) {
                input = "";
            }
            return input;
        }

        public Integer getVolume() {
            return volume;
        }

        public String getstatusUpdated() {
            if (statusUpdated == null) {
                statusUpdated = "";
            }
            return statusUpdated;
        }
    }

    public class NetUSB {
        @SerializedName("preset_control")
        private PresetControl presetControl;
        @SerializedName("play_info_updated")
        private String playInfoUpdated;

        public PresetControl getPresetControl() {
            return presetControl;
        }

        public String getPlayInfoUpdated() {
            if (playInfoUpdated == null) {
                playInfoUpdated = "";
            }
            return playInfoUpdated;
        }
    }

    public class PresetControl {
        @SerializedName("type")
        private String type;
        @SerializedName("num")
        private Integer num = Integer.valueOf(1);
        @SerializedName("result")
        private String result;

        public String getType() {
            if (type == null) {
                type = "";
            }
            return type;
        }

        public String getResult() {
            if (result == null) {
                result = "";
            }
            return result;
        }

        public Integer getNum() {
            return num;
        }
    }
}
