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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
/**
 * The {@link UdpServiceImpl} implements UdpService
 * handlers.
 *
 * @author Lennert Coopman - Initial contribution
 */

@NonNullByDefault
@Component(configurationPid = "binding.yamahamusiccast", service = UdpService.class)
public class UdpServiceImpl implements UdpService {
    private final Logger logger = LoggerFactory.getLogger(UdpServiceImpl.class);

    private static final int UDP_PORT = 41100;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;
    private static final int BUFFER_SIZE = 5120;
    private @Nullable DatagramSocket socket;
    //private InetAddress inetAddress;

    @Activate
    public UdpServiceImpl() {
        modified();
    }

    @Modified
    protected void modified() {
        logger.info("modified started");
        
        try {
            DatagramSocket s = new DatagramSocket(null);
            s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            s.setReuseAddress(true);
            InetSocketAddress address = new InetSocketAddress(UDP_PORT);
            s.bind(address);
            socket = s;
            logger.debug("Listener got UDP socket on port {} with timeout {}", UDP_PORT, SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.debug("Listener got SocketException: {}", e.getMessage(), e);
            socket = null;
            return;
        }

        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        while (socket != null) {
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                logger.info("received packet: {}", received);
            } catch (SocketTimeoutException e) {
                // Nothing to do on socket timeout
            } catch (IOException e) {
                logger.debug("Listener got IOException waiting for datagram: {}", e.getMessage());
                socket = null;
            }
        }
        logger.debug("Listener exiting");

    }

    @Deactivate
    public void deactivate() {
        logger.info("deactivate");
        if (socket != null) {
            socket.close();
            logger.debug("Listener closing listener socket");
            socket = null;
        }
    }

    @Override
    public void doSomething() {
        logger.info("doSomehting");
    }
}
