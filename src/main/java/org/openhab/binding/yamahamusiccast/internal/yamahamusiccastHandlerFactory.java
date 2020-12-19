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

import static org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link yamahamusiccastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.yamahamusiccast", service = ThingHandlerFactory.class)
public class YamahaMusiccastHandlerFactory extends BaseThingHandlerFactory {

    // private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_DEVICE);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(YamahaMusiccastBindingConstants.THING_DEVICE);
        SUPPORTED_THING_TYPES_UIDS.add(YamahaMusiccastBindingConstants.THING_TYPE_BRIDGE);
    }

    private final YamahaMusiccastStateDescriptionProvider stateDescriptionProvider;
    // private final UdpService udpService;

    @Activate
    public YamahaMusiccastHandlerFactory(@Reference YamahaMusiccastStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        // this.udpService = udpService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        // return THING_TYPE_BRIDGE.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            // YamahaMusiccastBridgeHandler bridgeHandler = new YamahaMusiccastBridgeHandler(thing);
            // return bridgeHandler;
            return new YamahaMusiccastBridgeHandler((Bridge) thing);
        } else if (THING_DEVICE.equals(thingTypeUID)) {
            return new YamahaMusiccastHandler(thing, stateDescriptionProvider);
        }
        return null;
    }
}
