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
package net.raphimc.raknetproviders.relativitymc_netty_raknet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.MessageToMessageCodec;
import net.raphimc.netminecraft.constants.ConnectionState;
import net.raphimc.netminecraft.util.ChannelType;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.vialoader.netty.VLPipeline;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.proxy.session.BedrockProxyConnection;
import net.raphimc.viaproxy.proxy.session.ProxyConnection;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.client.RakNetClient;
import network.ycc.raknet.client.channel.RakNetClientThreadedChannel;
import network.ycc.raknet.frame.FrameData;
import network.ycc.raknet.packet.FramedPacket;
import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.util.List;

public class RelativityMcNettyRakNetBedrockProxyConnection extends BedrockProxyConnection {

    public RelativityMcNettyRakNetBedrockProxyConnection(final RelativityMcNettyRakNetBedrockProxyConnection bedrockProxyConnection) {
        super(bedrockProxyConnection.channelInitializer, bedrockProxyConnection.getC2P());
    }

    @Override
    public void initialize(ChannelType channelType, Bootstrap bootstrap) {
        if (this.getC2pConnectionState() == ConnectionState.LOGIN) {
            bootstrap
                    .group(channelType.clientEventLoopGroup().get())
                    .channel(RakNetClient.THREADED_CHANNEL)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ViaProxy.getConfig().getConnectTimeout())
                    .option(RakNet.PROTOCOL_VERSION, ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION)
                    .attr(ProxyConnection.PROXY_CONNECTION_ATTRIBUTE_KEY, this)
                    .handler(new ChannelInitializer<>() {

                        @Override
                        protected void initChannel(Channel channel) {
                            final RakNetClientThreadedChannel rakChannel = (RakNetClientThreadedChannel) channel;
                            rakChannel.config().setprotocolVersions(new int[]{ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION});
                            channel.pipeline().addLast(channelInitializer);

                            channel.pipeline().addBefore(VLPipeline.VIABEDROCK_FRAME_ENCAPSULATION_HANDLER_NAME, "viabedrock-frame-converter", new MessageToMessageCodec<FrameData, RakMessage>() {
                                @Override
                                protected void encode(ChannelHandlerContext channelHandlerContext, RakMessage rakMessage, List<Object> list) {
                                    final FrameData frameData = FrameData.read(rakMessage.content(), rakMessage.content().readableBytes(), false);
                                    frameData.setReliability(FramedPacket.Reliability.get(rakMessage.reliability().ordinal()));
                                    frameData.setOrderChannel(rakMessage.channel());
                                    list.add(frameData);
                                }

                                @Override
                                protected void decode(ChannelHandlerContext channelHandlerContext, FrameData frameData, List<Object> list) {
                                    final RakReliability reliability = RakReliability.fromId(frameData.getReliability().ordinal());
                                    list.add(new RakMessage(frameData.createData(), reliability, RakPriority.NORMAL, frameData.getOrderChannel()));
                                }
                            });
                        }
                    });

            this.channelFuture = bootstrap.register().syncUninterruptibly();
        } else {
            super.initialize(channelType, bootstrap);
        }
    }

}
