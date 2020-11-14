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

public class Status {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("power")
    private String power;

    @SerializedName("mute")
    private String mute;

    @SerializedName("volume")
    private Integer volume;

    @SerializedName("max_volume")
    private Integer maxVolume;

    @SerializedName("input")
    private String input;

    @SerializedName("sound_program")
    private String soundProgram;

    @SerializedName("sleep")
    private Integer sleep = Integer.valueOf(0);


    public String getResponseCode() {
        return responseCode;
    }

    public String getPower() {
        return power;
    }

    public String getMute() {
        return mute;
    }

    public Integer getVolume() {
        return volume;
    }

    public Integer getMaxVolume() {
        return maxVolume;
    }

    public String getInput() {
        return input;
    }

    public String getSoundProgram() {
        return soundProgram;
    }

    public Integer getSleep() {
        return sleep;
    }

}


