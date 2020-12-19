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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahamusiccast.internal.model.UdpMessage;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link YamahaMusiccastBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(YamahaMusiccastBridgeHandler.class);

    private final ScheduledExecutorService UdpScheduler = ThreadPoolManager
            .getScheduledPool("YamahaMusiccastListener" + "-" + thing.getUID().getId());
    private @Nullable ScheduledFuture<?> listenerJob;
    private final UdpListener udpListener;

    public YamahaMusiccastBridgeHandler(Bridge bridge) {
        super(bridge);
        udpListener = new UdpListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        startUDPListenerJob();
    }

    @Override
    public void dispose() {
        stopUDPListenerJob();
        super.dispose();
    }

    private void startUDPListenerJob() {
        logger.info("YXC - Bridge Listener to start in 5 seconds");
        listenerJob = UdpScheduler.schedule(udpListener, 5, TimeUnit.SECONDS);
    }

    private void stopUDPListenerJob() {
        if (listenerJob != null) {
            listenerJob.cancel(true);
            udpListener.shutdown();
            logger.debug("Canceling listener job");
        }
    }

    public void handleUDPEvent(String json) {
        String udpDeviceId = "";
        Bridge bridge = (Bridge) thing;
        for (Thing thing : bridge.getThings()) {
            YamahaMusiccastHandler handler = (YamahaMusiccastHandler) thing.getHandler();
            logger.debug("UDP: {} - {} ({})", json, handler.getDeviceId(), thing.getLabel());
            try {
                UdpMessage targetObject = new UdpMessage();
                targetObject = new Gson().fromJson(json, UdpMessage.class);
                udpDeviceId = targetObject.getDeviceId();
            } catch (Exception e) {
                logger.warn("Error fetching Device Id");
                udpDeviceId = "";
            }
            if (udpDeviceId.equals(handler.getDeviceId())) {
                // logger.info("package for {}", thing.getLabel());
                handler.processUDPEvent(json);
            }
        }
    }
}
