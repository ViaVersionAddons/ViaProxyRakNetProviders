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
package net.raphimc.raknetproviders.cloudburstmc_network;

import com.viaversion.vialoader.netty.VLPipeline;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import net.raphimc.netminecraft.constants.ConnectionState;
import net.raphimc.netminecraft.util.EventLoops;
import net.raphimc.netminecraft.util.TransportType;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.proxy.session.BedrockProxyConnection;
import net.raphimc.viaproxy.proxy.session.ProxyConnection;
import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.cloudburstmc.upstream.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.upstream.netty.channel.raknet.config.RakChannelOption;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CloudburstMcNetworkBedrockProxyConnection extends BedrockProxyConnection {

    public CloudburstMcNetworkBedrockProxyConnection(final CloudburstMcNetworkBedrockProxyConnection bedrockProxyConnection) {
        super(bedrockProxyConnection.channelInitializer, bedrockProxyConnection.getC2P());
    }

    @Override
    public void initialize(TransportType transportType, Bootstrap bootstrap) {
        if (this.getC2pConnectionState() == ConnectionState.LOGIN) {
            if (!DatagramChannel.class.isAssignableFrom(transportType.udpClientChannelClass())) {
                throw new IllegalArgumentException("Transport type channel must be a DatagramChannel");
            }
            if (transportType == TransportType.KQUEUE) transportType = TransportType.NIO; // KQueue doesn't work for Bedrock for some reason
            final Class<? extends DatagramChannel> channelClass = (Class<? extends DatagramChannel>) transportType.udpClientChannelClass();

            bootstrap
                    .group(EventLoops.getClientEventLoop(transportType))
                    .channelFactory(RakChannelFactory.client(channelClass))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ViaProxy.getConfig().getConnectTimeout())
                    .option(RakChannelOption.RAK_PROTOCOL_VERSION, ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION)
                    .option(RakChannelOption.RAK_COMPATIBILITY_MODE, true)
                    .option(RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES, 20)
                    .option(RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS, 500)
                    .option(RakChannelOption.RAK_CONNECT_TIMEOUT, (long) ViaProxy.getConfig().getConnectTimeout())
                    .option(RakChannelOption.RAK_SESSION_TIMEOUT, 30_000L)
                    .option(RakChannelOption.RAK_GUID, ThreadLocalRandom.current().nextLong())
                    .attr(ProxyConnection.PROXY_CONNECTION_ATTRIBUTE_KEY, this)
                    .handler(new ChannelInitializer<>() {

                        @Override
                        protected void initChannel(Channel channel) {
                            channel.pipeline().addLast(channelInitializer);

                            channel.pipeline().addBefore(VLPipeline.VIABEDROCK_RAKNET_MESSAGE_CODEC_NAME, "viabedrock-frame-converter", new MessageToMessageCodec<org.cloudburstmc.upstream.netty.channel.raknet.packet.RakMessage, RakMessage>() {
                                @Override
                                protected void encode(ChannelHandlerContext channelHandlerContext, RakMessage rakMessage, List<Object> list) {
                                    list.add(new org.cloudburstmc.upstream.netty.channel.raknet.packet.RakMessage(
                                            rakMessage.content().retain(),
                                            org.cloudburstmc.upstream.netty.channel.raknet.RakReliability.valueOf(rakMessage.reliability().name()),
                                            org.cloudburstmc.upstream.netty.channel.raknet.RakPriority.valueOf(rakMessage.priority().name()),
                                            rakMessage.channel()
                                    ));
                                }

                                @Override
                                protected void decode(ChannelHandlerContext channelHandlerContext, org.cloudburstmc.upstream.netty.channel.raknet.packet.RakMessage rakMessage, List<Object> list) {
                                    list.add(new RakMessage(
                                            rakMessage.content().retain(),
                                            RakReliability.valueOf(rakMessage.reliability().name()),
                                            RakPriority.valueOf(rakMessage.priority().name()),
                                            rakMessage.channel()
                                    ));
                                }
                            });
                        }

                    });

            this.channelFuture = bootstrap.register().syncUninterruptibly();

        /*if (this.getChannel().config().setOption(RakChannelOption.RAK_IP_DONT_FRAGMENT, true)) {
            this.getChannel().config().setOption(RakChannelOption.RAK_MTU_SIZES, new Integer[]{1492, 1200, 576});
        }*/
        } else {
            super.initialize(transportType, bootstrap);
        }
    }

}
