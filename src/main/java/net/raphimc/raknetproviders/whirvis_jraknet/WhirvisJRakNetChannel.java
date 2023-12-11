/*
 * This file is part of ViaProxyRakNetProviders - https://github.com/ViaVersionAddons/ViaProxyRakNetProviders
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.raknetproviders.whirvis_jraknet;

import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.client.RakNetClient;
import com.whirvis.jraknet.client.RakNetClientListener;
import com.whirvis.jraknet.peer.RakNetServerPeer;
import com.whirvis.jraknet.protocol.Reliability;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.util.internal.StringUtil;
import net.raphimc.raknetproviders.SimpleOioMessageChannel;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

public class WhirvisJRakNetChannel extends SimpleOioMessageChannel implements RakNetClientListener {

    private RakNetClient client;
    private Throwable exceptionToThrow;
    private final List<RakMessage> pendingReads = new LinkedList<>();

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        super.doConnect(remoteAddress, localAddress);

        this.client = new RakNetClient((InetSocketAddress) localAddress);
        if (this.client.getProtocolVersion() != ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION) {
            throw new IllegalStateException("RakNet protocol version mismatch: " + this.client.getProtocolVersion() + " != " + ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION);
        }
        this.client.addListener(this);
        this.client.connect((InetSocketAddress) remoteAddress);

        if (!this.client.isLoggedIn()) {
            throw new ConnectException("Failed to connect to server");
        }
    }

    @Override
    protected int doReadMessages(List<Object> list) {
        int readBytes = 0;
        synchronized (this.pendingReads) {
            for (RakMessage rakMessage : this.pendingReads) {
                readBytes += rakMessage.content().readableBytes();
                list.add(rakMessage);
            }
            this.pendingReads.clear();
        }

        if (this.exceptionToThrow != null) {
            final Throwable throwable = this.exceptionToThrow;
            this.exceptionToThrow = null;
            throw new RuntimeException(throwable);
        }

        return readBytes;
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) {
        while (true) {
            final Object msg = channelOutboundBuffer.current();
            if (msg == null) {
                return;
            } else if (msg instanceof RakMessage rakMessage) {
                // Packet#array() can't handle direct buffers, so we have to copy it to a heap buffer.
                final ByteBuf whatAGreatLibThatCantHandleDirectBuffers = Unpooled.copiedBuffer(rakMessage.content());
                this.client.sendMessage(Reliability.RELIABLE_ORDERED, whatAGreatLibThatCantHandleDirectBuffers);
                channelOutboundBuffer.remove();
            } else {
                channelOutboundBuffer.remove(new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg)));
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();

        this.client.disconnect();
        this.client = null;
    }

    @Override
    public boolean isActive() {
        return this.client != null && this.client.isLoggedIn();
    }

    @Override
    public void handleMessage(RakNetClient client, RakNetServerPeer peer, RakNetPacket packet, int channel) {
        final ByteBuf buf = packet.buffer();
        buf.resetReaderIndex();
        synchronized (this.pendingReads) {
            this.pendingReads.add(new RakMessage(buf));
        }
    }

    @Override
    public void onDisconnect(RakNetClient client, InetSocketAddress address, RakNetServerPeer peer, String reason) {
        this.disconnect();
    }

    @Override
    public void onHandlerException(RakNetClient client, InetSocketAddress address, Throwable throwable) {
        if (this.exceptionToThrow == null) {
            this.exceptionToThrow = throwable;
        } else {
            this.exceptionToThrow.addSuppressed(throwable);
        }
    }

    @Override
    public void onPeerException(RakNetClient client, RakNetServerPeer peer, Throwable throwable) {
        if (this.exceptionToThrow == null) {
            this.exceptionToThrow = throwable;
        } else {
            this.exceptionToThrow.addSuppressed(throwable);
        }
    }

}
