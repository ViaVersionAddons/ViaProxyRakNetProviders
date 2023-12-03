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
package net.raphimc.raknetproviders.raknetnative;

import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.FileRegion;
import io.netty.channel.oio.AbstractOioByteChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RakNetNativeChannel extends AbstractOioByteChannel {

    private final ChannelConfig config = new DefaultChannelConfig(this);

    private SocketAddress remoteAddress;
    private SocketAddress localAddress;

    private Pointer rakPeer;

    private boolean open = true;
    private boolean active = false;

    public RakNetNativeChannel() {
        super(null);
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;

        this.rakPeer = RakNetNative.INSTANCE.RN_RakPeerGetInstance();
        if (RakNetNative.INSTANCE.RN_RakPeerStartup(this.rakPeer, 1, new RakNetNative.RN_SocketDescriptor[]{new RakNetNative.RN_SocketDescriptor((short) 0, "0.0.0.0", (short) 2, (short) 0, 0, false, 0)}, 1, -99999) != 0) {
            throw new RuntimeException("Failed to start RakPeer");
        }
        if (RakNetNative.INSTANCE.RN_RakPeerConnect(this.rakPeer, ((InetSocketAddress) remoteAddress).getHostString(), (short) ((InetSocketAddress) remoteAddress).getPort(), null, 0, null, 0, 12, 500, 0) != 0) {
            throw new RuntimeException("Failed to connect to server");
        }

        this.active = true;
    }

    @Override
    protected int doReadBytes(ByteBuf buf) {
        final RakNetNative.RN_Packet packet = RakNetNative.INSTANCE.RN_RakPeerReceive(this.rakPeer);
        if (packet == null) {
            return 0;
        }
        if (packet.data.getByte(0) != (byte) 0xFE) {
            RakNetNative.INSTANCE.RN_RakPeerDeallocatePacket(this.rakPeer, packet);
            return 0;
        }
        final byte[] bytes = packet.data.getByteArray(1, packet.length - 1);
        buf.writeBytes(bytes);

        RakNetNative.INSTANCE.RN_RakPeerDeallocatePacket(this.rakPeer, packet);

        return bytes.length;
    }

    @Override
    protected void doWriteBytes(ByteBuf buf) {
        final byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        final RakNetNative.RN_AddressOrGUID.ByValue address = new RakNetNative.RN_AddressOrGUID.ByValue();
        address.guid = -1;
        if (RakNetNative.INSTANCE.RN_RakPeerSend(this.rakPeer, bytes, bytes.length, 2, 3, (byte) 0, address, true, 0) == 0) {
            throw new RuntimeException("Failed to send packet");
        }
    }

    @Override
    protected void doWriteFileRegion(FileRegion region) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SocketAddress localAddress0() {
        return this.localAddress;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return this.remoteAddress;
    }

    @Override
    protected int available() {
        return 0;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ChannelFuture shutdownInput() {
        this.doClose();
        return this.newSucceededFuture();
    }

    @Override
    protected void doDisconnect() {
        this.doClose();
    }

    @Override
    protected void doClose() {
        this.open = false;

        RakNetNative.INSTANCE.RN_RakPeerShutdown(this.rakPeer, 500, 0, 2);
        RakNetNative.INSTANCE.RN_RakPeerDestroyInstance(this.rakPeer);
        this.rakPeer = null;
    }

    @Override
    public ChannelConfig config() {
        return this.config;
    }

    @Override
    protected boolean isInputShutdown() {
        return !this.open;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean isActive() {
        return this.active && this.open;
    }

}
