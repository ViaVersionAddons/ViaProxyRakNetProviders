/*
 * This file is part of ViaProxyRakNetProviders - https://github.com/ViaVersionAddons/ViaProxyRakNetProviders
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.raknetproviders.b23r0_rust_raknet;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.util.internal.StringUtil;
import net.raphimc.raknetproviders.SimpleOioMessageChannel;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class B23R0RustRakNetChannel extends SimpleOioMessageChannel {

    protected boolean active = false;

    private Pointer connection;
    private final Queue<Object> readQueue = new LinkedTransferQueue<>();
    private final Callback callback = new Callback() {
        public void onRead(final String readError, final Pointer data, final int length) {
            if (readError != null) {
                readQueue.add(readError);
            } else {
                readQueue.add(data.getByteArray(0, length));
            }
        }
    };

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        super.doConnect(remoteAddress, localAddress);

        final InetSocketAddress address = (InetSocketAddress) remoteAddress;
        final PointerByReference connectionPointer = new PointerByReference();
        final String connectError = B23R0RustRakNet.INSTANCE.connect(address.getHostString() + ":" + address.getPort(), this.config().getConnectTimeoutMillis(), (byte) ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION, connectionPointer);
        if (connectError != null) {
            throw new ConnectException(connectError);
        }
        this.connection = connectionPointer.getValue();

        B23R0RustRakNet.INSTANCE.begin_receive_packet(this.connection, 30_000, this.callback);

        this.active = true;
    }

    @Override
    protected int doReadMessages(List<Object> list) {
        if (this.connection == null) {
            return -1;
        }
        if (!this.readQueue.isEmpty()) {
            final Object obj = this.readQueue.poll();
            if (obj instanceof byte[] bytes) {
                list.add(new RakMessage(this.alloc().buffer(bytes.length).writeBytes(bytes)));
                return bytes.length;
            } else if (obj instanceof String errorMessage) {
                throw new RuntimeException(errorMessage);
            }
        }

        return 0;
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

                final String writeError = B23R0RustRakNet.INSTANCE.send_packet(this.connection, bytes, bytes.length);
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
            final String disconnectError = B23R0RustRakNet.INSTANCE.disconnect(this.connection);
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
