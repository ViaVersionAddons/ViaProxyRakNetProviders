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
package net.raphimc.raknetproviders.util;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Map;

public abstract class SimpleOioDatagramChannel extends SimpleOioMessageChannel implements DatagramChannel {

    private final DatagramChannelConfig config = new Config();

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public DatagramChannelConfig config() {
        return this.config;
    }

    @Override
    public boolean isConnected() {
        return this.isActive();
    }

    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture joinGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture joinGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress sourceToBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress sourceToBlock, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final InetAddress sourceToBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final InetAddress sourceToBlock, final ChannelPromise future) {
        throw new UnsupportedOperationException();
    }

    private class Config implements DatagramChannelConfig {

        @Override
        public int getSendBufferSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setSendBufferSize(final int sendBufferSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getReceiveBufferSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setReceiveBufferSize(final int receiveBufferSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTrafficClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setTrafficClass(final int trafficClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReuseAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setReuseAddress(final boolean reuseAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isBroadcast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setBroadcast(final boolean broadcast) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLoopbackModeDisabled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setLoopbackModeDisabled(final boolean loopbackModeDisabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTimeToLive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setTimeToLive(final int ttl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InetAddress getInterface() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setInterface(final InetAddress interfaceAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public NetworkInterface getNetworkInterface() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setNetworkInterface(final NetworkInterface networkInterface) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatagramChannelConfig setMaxMessagesPerRead(final int maxMessagesPerRead) {
            SimpleOioDatagramChannel.super.config().setMaxMessagesPerRead(maxMessagesPerRead);
            return this;
        }

        @Override
        public int getWriteSpinCount() {
            return SimpleOioDatagramChannel.super.config().getWriteSpinCount();
        }

        @Override
        public DatagramChannelConfig setWriteSpinCount(final int writeSpinCount) {
            SimpleOioDatagramChannel.super.config().setWriteSpinCount(writeSpinCount);
            return this;
        }

        @Override
        public ByteBufAllocator getAllocator() {
            return SimpleOioDatagramChannel.super.config().getAllocator();
        }

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return SimpleOioDatagramChannel.super.config().getOptions();
        }

        @Override
        public boolean setOptions(final Map<ChannelOption<?>, ?> options) {
            return SimpleOioDatagramChannel.super.config().setOptions(options);
        }

        @Override
        public <T> T getOption(final ChannelOption<T> option) {
            return SimpleOioDatagramChannel.super.config().getOption(option);
        }

        @Override
        public <T> boolean setOption(final ChannelOption<T> option, final T value) {
            return SimpleOioDatagramChannel.super.config().setOption(option, value);
        }

        @Override
        public int getConnectTimeoutMillis() {
            return SimpleOioDatagramChannel.super.config().getConnectTimeoutMillis();
        }

        @Override
        public DatagramChannelConfig setConnectTimeoutMillis(final int connectTimeoutMillis) {
            SimpleOioDatagramChannel.super.config().setConnectTimeoutMillis(connectTimeoutMillis);
            return this;
        }

        @Override
        public int getMaxMessagesPerRead() {
            return SimpleOioDatagramChannel.super.config().getMaxMessagesPerRead();
        }

        @Override
        public DatagramChannelConfig setAllocator(final ByteBufAllocator allocator) {
            SimpleOioDatagramChannel.super.config().setAllocator(allocator);
            return this;
        }

        @Override
        public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator() {
            return SimpleOioDatagramChannel.super.config().getRecvByteBufAllocator();
        }

        @Override
        public DatagramChannelConfig setRecvByteBufAllocator(final RecvByteBufAllocator allocator) {
            SimpleOioDatagramChannel.super.config().setRecvByteBufAllocator(allocator);
            return this;
        }

        @Override
        public boolean isAutoRead() {
            return SimpleOioDatagramChannel.super.config().isAutoRead();
        }

        @Override
        public DatagramChannelConfig setAutoRead(final boolean autoRead) {
            SimpleOioDatagramChannel.super.config().setAutoRead(autoRead);
            return this;
        }

        @Override
        public boolean isAutoClose() {
            return SimpleOioDatagramChannel.super.config().isAutoClose();
        }

        @Override
        public DatagramChannelConfig setAutoClose(final boolean autoClose) {
            SimpleOioDatagramChannel.super.config().setAutoClose(autoClose);
            return this;
        }

        @Override
        public int getWriteBufferHighWaterMark() {
            return SimpleOioDatagramChannel.super.config().getWriteBufferHighWaterMark();
        }

        @Override
        public ChannelConfig setWriteBufferHighWaterMark(final int writeBufferHighWaterMark) {
            SimpleOioDatagramChannel.super.config().setWriteBufferHighWaterMark(writeBufferHighWaterMark);
            return this;
        }

        @Override
        public int getWriteBufferLowWaterMark() {
            return SimpleOioDatagramChannel.super.config().getWriteBufferLowWaterMark();
        }

        @Override
        public ChannelConfig setWriteBufferLowWaterMark(final int writeBufferLowWaterMark) {
            SimpleOioDatagramChannel.super.config().setWriteBufferLowWaterMark(writeBufferLowWaterMark);
            return this;
        }

        @Override
        public MessageSizeEstimator getMessageSizeEstimator() {
            return SimpleOioDatagramChannel.super.config().getMessageSizeEstimator();
        }

        @Override
        public DatagramChannelConfig setMessageSizeEstimator(final MessageSizeEstimator estimator) {
            SimpleOioDatagramChannel.super.config().setMessageSizeEstimator(estimator);
            return this;
        }

        @Override
        public WriteBufferWaterMark getWriteBufferWaterMark() {
            return SimpleOioDatagramChannel.super.config().getWriteBufferWaterMark();
        }

        @Override
        public DatagramChannelConfig setWriteBufferWaterMark(final WriteBufferWaterMark writeBufferWaterMark) {
            SimpleOioDatagramChannel.super.config().setWriteBufferWaterMark(writeBufferWaterMark);
            return this;
        }

    }

}
