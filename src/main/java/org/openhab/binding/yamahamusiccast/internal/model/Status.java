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
    private String ResponseCode;

    @SerializedName("power")
    private String Power;

    @SerializedName("mute")
    private String Mute;

    @SerializedName("volume")
    private Integer Volume;

    @SerializedName("max_volume")
    private Integer MaxVolume;

    @SerializedName("input")
    private String Input;

    @SerializedName("sound_program")
    private String SoundProgram;

    @SerializedName("sleep")
    private Integer Sleep = Integer.valueOf(0);


    public String getResponseCode() {
        return ResponseCode;
    }

    public String getPower() {
        return Power;
    }

    public String getMute() {
        return Mute;
    }

    public Integer getVolume() {
        return Volume;
    }

    public Integer getMaxVolume() {
        return MaxVolume;
    }

    public String getInput() {
        return Input;
    }

    public String getSoundProgram() {
        return SoundProgram;
    }

    public Integer getSleep() {
        return Sleep;
    }

}


