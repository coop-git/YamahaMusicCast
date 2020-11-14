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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;


/**
 * This class represents the push request sent to the API.
 *
 * @author Lennert Coopman - Initial contribution
 */

public class ThingsRest {

    @SerializedName("thingTypeUID")
    private String thingTypeUID;
    
    @SerializedName("label")
    private String label;

    @SerializedName("configuration")
    private JsonObject configuration;


    public String getThingTypeUID() {
        return thingTypeUID;
    }
    
    public String getLabel() {
        return label;
    }

    public JsonObject getConfiguration() {
        return configuration;
    }


}