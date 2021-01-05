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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the PlayInfo request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class PlayInfo {

    @SerializedName("response_code")
    private @Nullable String responseCode;

    @SerializedName("playback")
    private @Nullable String playback;

    @SerializedName("artist")
    private @Nullable String artist;

    @SerializedName("track")
    private @Nullable String track;

    @SerializedName("album")
    private @Nullable String album;

    @SerializedName("albumart_url")
    private @Nullable String albumarturl;

    @SerializedName("repeat")
    private @Nullable String repeat;

    @SerializedName("shuffle")
    private @Nullable String shuffle;


    public @Nullable String getResponseCode() {
        return responseCode;
    }

    public @Nullable String getPlayback() {
        if (playback==null) {playback = "";}
        return playback;
    }  

    public @Nullable String getArtist() {
        if (artist==null) {artist = "";}
        return artist;
    }  

    public @Nullable String getTrack() {
        if (track==null) {track = "";}
        return track;
    }  

    public @Nullable String getAlbum() {
        if (album==null) {album = "";}
        return album;
    }  
   
    public @Nullable String getAlbumArtUrl() {
        if (albumarturl==null) {albumarturl = "";}
        return albumarturl;
    }  

    public @Nullable String getRepeat() {
        if (repeat==null) {repeat = "";}
        return repeat;
    }  

    public @Nullable String getShuffle() {
        if (shuffle==null) {shuffle = "";}
        return shuffle;
    }  
   


}


