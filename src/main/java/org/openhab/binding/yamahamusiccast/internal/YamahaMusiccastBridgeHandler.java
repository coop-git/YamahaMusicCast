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

import org.openhab.binding.yamahamusiccast.internal.dto.UdpMessage;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.common.NamedThreadFactory;
import org.openhab.binding.yamahamusiccast.internal.UdpListener;
import java.io.IOException;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.UUID;


/**
 * The {@link YamahaMusiccastBridgeHandler} is responsible for dispatching UDP events to linked Things.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBridgeHandler extends BaseBridgeHandler {
    private Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(YamahaMusiccastBridgeHandler.class);
    private String threadname = getThing().getUID().getAsString(); //"binding-yamahamusiccast" 
    private @Nullable ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(threadname));
    private @Nullable Future<?> eventListenerJob;
    private static final int UDP_PORT = 41100;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;
    private static final int BUFFER_SIZE = 5120;
    private @Nullable DatagramSocket socket;

    private void receivePackets() {
        try {
            DatagramSocket s = new DatagramSocket(null);
            s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            s.setReuseAddress(true);
            InetSocketAddress address = new InetSocketAddress(UDP_PORT);
            s.bind(address);
            socket = s;
            logger.debug("UDP Listener got socket on port {} with timeout {}", UDP_PORT, SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.debug("UDP Listener got SocketException: {}", e.getMessage(), e);
            socket = null;
            return;
        }

        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        while (socket != null) {
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String trackingID = UUID.randomUUID().toString().replace("-","").substring(0,32);
                logger.debug("Received packet: {} (Tracking: {})", received, trackingID);
                handleUDPEvent(received,trackingID);
            } catch (SocketTimeoutException e) {
                // Nothing to do on socket timeout
            } catch (IOException e) {
                logger.debug("UDP Listener got IOException waiting for datagram: {}", e.getMessage());
                socket = null;
            }
        }
        logger.debug("UDP Listener exiting");
    }

    public YamahaMusiccastBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        Future<?> localEventListenerJob = eventListenerJob;
        //String threadname = getThing().getUID().getAsString(); //"binding-yamahamusiccast"
        if (localEventListenerJob == null || localEventListenerJob.isCancelled()) {
            //executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(threadname));
            localEventListenerJob = executor.submit(this::receivePackets);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        Future<?> localEventListenerJob = eventListenerJob;
        if (localEventListenerJob != null) {
            localEventListenerJob.cancel(true);
            localEventListenerJob = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void handleUDPEvent(String json, String trackingID) {
        String udpDeviceId = "";
        Bridge bridge = (Bridge) thing;
        for (Thing thing : bridge.getThings()) {
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            switch (statusInfo.getStatus()) {
                case ONLINE:
                    logger.debug("Thing Status: ONLINE - {}",thing.getLabel());
                    YamahaMusiccastHandler handler = (YamahaMusiccastHandler) thing.getHandler();
                    logger.debug("UDP: {} - {} ({} - Tracking: {})", json, handler.getDeviceId(), thing.getLabel(), trackingID);

                    @Nullable
                    UdpMessage targetObject = gson.fromJson(json, UdpMessage.class);
                    udpDeviceId = targetObject.getDeviceId();
                    if (udpDeviceId.equals(handler.getDeviceId())) {
                        handler.processUDPEvent(json, trackingID);
                    }
                    break;
                default:
                    logger.debug("Thing Status: NOT ONLINE - {} (Tracking: {})",thing.getLabel(), trackingID);
                    break;
            }
        }
    }

}