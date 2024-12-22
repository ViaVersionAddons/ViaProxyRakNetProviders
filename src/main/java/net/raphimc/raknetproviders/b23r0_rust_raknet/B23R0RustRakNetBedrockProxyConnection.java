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
package net.raphimc.raknetproviders.b23r0_rust_raknet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.oio.OioEventLoopGroup;
import net.raphimc.netminecraft.constants.ConnectionState;
import net.raphimc.netminecraft.util.ChannelType;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.proxy.session.BedrockProxyConnection;
import net.raphimc.viaproxy.proxy.session.ProxyConnection;

public class B23R0RustRakNetBedrockProxyConnection extends BedrockProxyConnection {

    public B23R0RustRakNetBedrockProxyConnection(final B23R0RustRakNetBedrockProxyConnection bedrockProxyConnection) {
        super(bedrockProxyConnection.channelInitializer, bedrockProxyConnection.getC2P());
    }

    @Override
    public void initialize(ChannelType channelType, Bootstrap bootstrap) {
        if (this.getC2pConnectionState() == ConnectionState.LOGIN) {
            bootstrap
                    .group(new OioEventLoopGroup())
                    .channel(B23R0RustRakNetChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ViaProxy.getConfig().getConnectTimeout())
                    .attr(ProxyConnection.PROXY_CONNECTION_ATTRIBUTE_KEY, this)
                    .handler(this.channelInitializer);

            this.channelFuture = bootstrap.register().syncUninterruptibly();
        } else {
            super.initialize(channelType, bootstrap);
        }
    }

}
