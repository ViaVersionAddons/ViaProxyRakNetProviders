/*
 * This file is part of ViaProxyRakNetProviders - https://github.com/ViaVersionAddons/ViaProxyRakNetProviders
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.raknetproviders.sandertv_go_raknet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.util.internal.StringUtil;
import net.raphimc.raknetproviders.SimpleOioMessageChannel;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class SanderTvGoRakNetChannel extends SimpleOioMessageChannel {

    protected boolean active = false;

    private Pointer connection;

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        super.doConnect(remoteAddress, localAddress);

        final InetSocketAddress address = (InetSocketAddress) remoteAddress;
        final PointerByReference connectionPointer = new PointerByReference();
        final String connectError = SanderTvGoRakNet.INSTANCE.connect(address.getHostString() + ":" + address.getPort(), this.config().getConnectTimeoutMillis(), connectionPointer);
        if (connectError != null) {
            throw new ConnectException(connectError);
        }
        this.connection = connectionPointer.getValue();

        this.active = true;
    }

    @Override
    protected int doReadMessages(List<Object> list) {
        if (this.connection == null) {
            return -1;
        }

        final PointerByReference dataPointer = new PointerByReference();
        final IntByReference lengthPointer = new IntByReference();
        final String readError = SanderTvGoRakNet.INSTANCE.receivePacket(this.connection, dataPointer, lengthPointer);
        if (readError != null) {
            throw new RuntimeException(readError);
        }
        if (dataPointer.getValue() == Pointer.NULL) {
            return 0;
        }

        final byte[] bytes = dataPointer.getValue().getByteArray(0, lengthPointer.getValue());
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

                final String writeError = SanderTvGoRakNet.INSTANCE.sendPacket(this.connection, bytes, bytes.length);
                if (writeError != null) {
                    throw new RuntimeException(writeError);
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

        if (this.connection != null) {
            final String disconnectError = SanderTvGoRakNet.INSTANCE.disconnect(this.connection);
            this.connection = null;
            if (disconnectError != null) {
                throw new RuntimeException(disconnectError);
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.active && this.open;
    }

}
