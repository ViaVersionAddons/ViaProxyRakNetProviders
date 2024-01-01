/*
 * This file is part of ViaProxyRakNetProviders - https://github.com/ViaVersionAddons/ViaProxyRakNetProviders
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.raknetproviders.native_raknet;

import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ConnectTimeoutException;
import io.netty.util.internal.StringUtil;
import net.raphimc.raknetproviders.SimpleOioMessageChannel;
import org.cloudburstmc.netty.channel.raknet.RakConstants;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class NativeRakNetChannel extends SimpleOioMessageChannel {

    protected boolean active = false;

    private Pointer rakPeer;

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        super.doConnect(remoteAddress, localAddress);

        this.rakPeer = NativeRakNet.INSTANCE.RN_RakPeerGetInstance();
        if (NativeRakNet.INSTANCE.RN_RakPeerStartup(this.rakPeer, 1, new NativeRakNet.RN_SocketDescriptor[]{new NativeRakNet.RN_SocketDescriptor((short) 0, "0.0.0.0", (short) 2, (short) 0, 0, false, 0)}, 1, -99999) != 0) {
            throw new ConnectException("Failed to start RakPeer");
        }
        if (NativeRakNet.INSTANCE.RN_RakPeerConnect(this.rakPeer, ((InetSocketAddress) remoteAddress).getHostString(), (short) ((InetSocketAddress) remoteAddress).getPort(), null, 0, null, 0, 12, 500, 0) != 0) {
            throw new ConnectException("Failed to connect to server");
        }

        final long start = System.currentTimeMillis();
        final long timeout = this.config().getConnectTimeoutMillis();
        while (System.currentTimeMillis() - start < timeout) {
            final NativeRakNet.RN_Packet packet = NativeRakNet.INSTANCE.RN_RakPeerReceive(this.rakPeer);
            if (packet == null) {
                Thread.sleep(50);
                continue;
            }
            try {
                if (packet.length <= 0) throw new ConnectException("Received empty packet");
                final int packetId = packet.data.getByte(0) & 0xFF;

                if (packetId == RakConstants.ID_CONNECTION_REQUEST_ACCEPTED) {
                    this.active = true;
                    break;
                } else {
                    throw new ConnectException("Received unexpected packet while connecting: " + packetId);
                }
            } finally {
                NativeRakNet.INSTANCE.RN_RakPeerDeallocatePacket(this.rakPeer, packet);
            }
        }

        if (!this.active) {
            throw new ConnectTimeoutException("Connect timed out");
        }
    }

    @Override
    protected int doReadMessages(List<Object> list) {
        final NativeRakNet.RN_Packet packet = NativeRakNet.INSTANCE.RN_RakPeerReceive(this.rakPeer);
        if (packet == null) {
            return 0;
        }
        final byte[] bytes = packet.data.getByteArray(0, packet.length);
        NativeRakNet.INSTANCE.RN_RakPeerDeallocatePacket(this.rakPeer, packet);
        list.add(new RakMessage(this.alloc().buffer(bytes.length).writeBytes(bytes)));
        return bytes.length;
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) {
        while (true) {
            final Object msg = channelOutboundBuffer.current();
            if (msg == null) {
                return;
            } else if (msg instanceof RakMessage rakMessage) {
                final ByteBuf buf = rakMessage.content();
                final byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);

                final NativeRakNet.RN_AddressOrGUID.ByValue address = new NativeRakNet.RN_AddressOrGUID.ByValue();
                address.guid = -1;
                if (NativeRakNet.INSTANCE.RN_RakPeerSend(this.rakPeer, bytes, bytes.length, rakMessage.priority().ordinal(), rakMessage.reliability().ordinal(), (byte) rakMessage.channel(), address, true, 0) == 0) {
                    throw new RuntimeException("Failed to send packet");
                }

                channelOutboundBuffer.remove();
            } else {
                channelOutboundBuffer.remove(new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg)));
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();

        NativeRakNet.INSTANCE.RN_RakPeerShutdown(this.rakPeer, 500, 0, 2);
        NativeRakNet.INSTANCE.RN_RakPeerDestroyInstance(this.rakPeer);
        this.rakPeer = null;
    }

    @Override
    public boolean isActive() {
        return this.active && this.open;
    }

}
