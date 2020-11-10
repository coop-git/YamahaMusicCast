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
package org.openhab.binding.yamahamusiccast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link yamahamusiccastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBindingConstants {

    private static final String BINDING_ID = "yamahamusiccast";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_DEVICE = new ThingTypeUID(BINDING_ID, "Device");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "channelPower";
    public static final String CHANNEL_MUTE = "channelMute";
    public static final String CHANNEL_VOLUME = "channelVolume";
    public static final String CHANNEL_INPUT = "channelInput";
    public static final String CHANNEL_SOUNDPROGRAM = "channelSoundProgram";
    public static final String CHANNEL_SELECTPRESET = "channelSelectPreset";
    public static final String CHANNEL_PLAYER = "channelPlayer";
    public static final String CHANNEL_SLEEP = "channelSleep";
    public static final String CHANNEL_MCSERVER = "channelMCServer";
    public static final String CHANNEL_UNLINKMCSERVER = "channelUnlinkMCServer";
    public static final String CHANNEL_RECALLSCENE = "channelRecallScene";
}
