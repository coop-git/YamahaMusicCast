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

public class DistributionInfo {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("role")
    private String role;

    @SerializedName("server_zone")
    private String serverZone;

    public String getResponseCode() {
        return responseCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getRole() {
        return role;
    }

    public String getServerZone() {
        return serverZone;
    }
}
