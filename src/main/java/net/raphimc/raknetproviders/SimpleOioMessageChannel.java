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
package net.raphimc.raknetproviders;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.oio.AbstractOioMessageChannel;

import java.net.SocketAddress;

public abstract class SimpleOioMessageChannel extends AbstractOioMessageChannel {

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private final ChannelConfig config = new DefaultChannelConfig(this);

    private SocketAddress remoteAddress;
    private SocketAddress localAddress;

    protected boolean open = true;

    protected SimpleOioMessageChannel() {
        super(null);
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
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
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        this.doClose();
    }

    @Override
    protected void doClose() throws Exception {
        this.open = false;
    }

    @Override
    public ChannelConfig config() {
        return this.config;
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

}
