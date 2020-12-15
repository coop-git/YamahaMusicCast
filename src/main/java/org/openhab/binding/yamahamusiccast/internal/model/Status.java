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
import org.eclipse.jdt.annotation.*;

/**
 * This class represents the push request sent to the API.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class Status {

    @SerializedName("response_code")
    private @Nullable String responseCode;

    @SerializedName("power")
    private @Nullable String power;

    @SerializedName("mute")
    private @Nullable String mute;

    @SerializedName("volume")
    private @Nullable Integer volume;

    @SerializedName("max_volume")
    private @Nullable Integer maxVolume;

    @SerializedName("input")
    private @Nullable String input;

    @SerializedName("sound_program")
    private @Nullable String soundProgram;

    @SerializedName("sleep")
    private @Nullable Integer sleep = Integer.valueOf(0);


    public @Nullable String getResponseCode() {
        return responseCode;
    }

    public @Nullable String getPower() {
        return power;
    }

    public @Nullable String getMute() {
        return mute;
    }

    public @Nullable Integer getVolume() {
        return volume;
    }

    public @Nullable Integer getMaxVolume() {
        return maxVolume;
    }

    public @Nullable String getInput() {
        return input;
    }

    public @Nullable String getSoundProgram() {
        return soundProgram;
    }

    public @Nullable Integer getSleep() {
        return sleep;
    }

}


