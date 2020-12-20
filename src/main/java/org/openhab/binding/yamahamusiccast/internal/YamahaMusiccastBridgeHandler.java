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

import org.openhab.binding.yamahamusiccast.internal.model.UdpMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.internal.jshell.tool.resources.version;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.yamahamusiccast.internal.UdpListener;


/**
 * The {@link YamahaMusiccastBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(YamahaMusiccastBridgeHandler.class);

    private final ScheduledExecutorService udpScheduler = ThreadPoolManager
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
        listenerJob = udpScheduler.schedule(udpListener, 5, TimeUnit.SECONDS);
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
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            switch (statusInfo.getStatus()) {
                case ONLINE:
                    logger.debug("Thing Status: ONLINE - {}",thing.getLabel());

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
                        //logger.info("package for {}", thing.getLabel());
                        handler.processUDPEvent(json);
                    }

                    break;
                default:
                    logger.debug("Thing Status: NOT ONLINE - {}",thing.getLabel());
                    break;
            }

            // YamahaMusiccastHandler handler = (YamahaMusiccastHandler) thing.getHandler();
            // logger.debug("UDP: {} - {} ({})", json, handler.getDeviceId(), thing.getLabel());
            // try {
            //     UdpMessage targetObject = new UdpMessage();
            //     targetObject = new Gson().fromJson(json, UdpMessage.class);
            //     udpDeviceId = targetObject.getDeviceId();
            // } catch (Exception e) {
            //     logger.warn("Error fetching Device Id");
            //     udpDeviceId = "";
            // }
            // if (udpDeviceId.equals(handler.getDeviceId())) {
            //     //logger.info("package for {}", thing.getLabel());
            //     handler.processUDPEvent(json);
            // }
        }
    }

}