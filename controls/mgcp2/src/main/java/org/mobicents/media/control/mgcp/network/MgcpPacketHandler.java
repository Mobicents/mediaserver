/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.control.mgcp.network;

import java.net.InetSocketAddress;

import org.mobicents.media.control.mgcp.exception.MgcpParseException;
import org.mobicents.media.control.mgcp.message.MessageDirection;
import org.mobicents.media.control.mgcp.message.MgcpMessage;
import org.mobicents.media.control.mgcp.message.MgcpMessageObserver;
import org.mobicents.media.control.mgcp.message.MgcpMessageParser;
import org.mobicents.media.control.mgcp.message.MgcpRequest;
import org.mobicents.media.control.mgcp.message.MgcpResponse;
import org.mobicents.media.server.io.network.channel.PacketHandler;
import org.mobicents.media.server.io.network.channel.PacketHandlerException;

/**
 * Handler to decode and process incoming MGCP packets.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class MgcpPacketHandler implements PacketHandler {

    public final MgcpMessageParser parser;
    public final MgcpMessageObserver listener;

    public MgcpPacketHandler(MgcpMessageParser parser, MgcpMessageObserver listener) {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public int compareTo(PacketHandler o) {
        if (o == null) {
            return 1;
        }
        return this.getPipelinePriority() - o.getPipelinePriority();
    }

    @Override
    public boolean canHandle(byte[] packet) {
        return canHandle(packet, packet.length, 0);
    }

    @Override
    public boolean canHandle(byte[] packet, int dataLength, int offset) {
        // TODO [MgcpPacketHandler] Check if packet can be handled somehow
        return true;
    }

    @Override
    public byte[] handle(byte[] packet, InetSocketAddress localPeer, InetSocketAddress remotePeer)
            throws PacketHandlerException {
        return handle(packet, packet.length, 0, localPeer, remotePeer);
    }

    @Override
    public byte[] handle(byte[] packet, int dataLength, int offset, InetSocketAddress localPeer, InetSocketAddress remotePeer)
            throws PacketHandlerException {
        // Get message type based on first byte
        byte b = packet[0];

        // Produce message according to type
        MgcpMessage message;
        if (b >= 48 && b <= 57) {
            message = handleResponse(packet, dataLength, offset, localPeer, remotePeer);
        } else {
            message = handleRequest(packet, dataLength, offset, localPeer, remotePeer);
        }

        // Warn listener packet was decoded
        if (this.listener != null) {
            this.listener.onMessage(message, MessageDirection.INCOMING);
        }
        return null;
    }

    private MgcpRequest handleRequest(byte[] packet, int dataLength, int offset, InetSocketAddress localPeer,
            InetSocketAddress remotePeer) throws PacketHandlerException {
        try {
            return this.parser.parseRequest(packet, offset, dataLength);
        } catch (MgcpParseException e) {
            throw new PacketHandlerException(e);
        }
    }

    private MgcpResponse handleResponse(byte[] packet, int dataLength, int offset, InetSocketAddress localPeer,
            InetSocketAddress remotePeer) throws PacketHandlerException {
        try {
            return this.parser.parseResponse(packet, offset, dataLength);
        } catch (MgcpParseException e) {
            throw new PacketHandlerException(e);
        }
    }

    @Override
    public int getPipelinePriority() {
        return 0;
    }

}
