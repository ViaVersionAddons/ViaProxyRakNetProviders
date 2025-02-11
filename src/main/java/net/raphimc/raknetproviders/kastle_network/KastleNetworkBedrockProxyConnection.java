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
package net.raphimc.raknetproviders.kastle_network;

import com.viaversion.vialoader.netty.VLPipeline;
import dev.kastle.netty.channel.raknet.RakChannelFactory;
import dev.kastle.netty.channel.raknet.RakPriority;
import dev.kastle.netty.channel.raknet.RakReliability;
import dev.kastle.netty.channel.raknet.config.RakChannelOption;
import dev.kastle.netty.channel.raknet.packet.RakMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.netminecraft.constants.ConnectionState;
import net.raphimc.netminecraft.util.ChannelType;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MinecraftPacketIds;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.proxy.session.BedrockProxyConnection;
import net.raphimc.viaproxy.proxy.session.ProxyConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KastleNetworkBedrockProxyConnection extends BedrockProxyConnection {

    public KastleNetworkBedrockProxyConnection(final KastleNetworkBedrockProxyConnection bedrockProxyConnection) {
        super(bedrockProxyConnection.channelInitializer, bedrockProxyConnection.getC2P());
    }

    @Override
    public void initialize(ChannelType channelType, Bootstrap bootstrap) {
        if (this.getC2pConnectionState() == ConnectionState.LOGIN) {
            if (!DatagramChannel.class.isAssignableFrom(channelType.udpClientChannelClass())) {
                throw new IllegalArgumentException("Channel type must be a DatagramChannel");
            }
            if (channelType == ChannelType.KQUEUE) channelType = ChannelType.NIO; // KQueue doesn't work for Bedrock for some reason
            final Class<? extends DatagramChannel> channelClass = (Class<? extends DatagramChannel>) channelType.udpClientChannelClass();

            // Reflection to prevent inlining
            final int bedrockProtocolVersion = RStream.of(ProtocolConstants.class).fields().by("BEDROCK_PROTOCOL_VERSION").get();

            bootstrap
                    .group(channelType.clientEventLoopGroup().get())
                    .channelFactory(RakChannelFactory.client(channelClass))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ViaProxy.getConfig().getConnectTimeout())
                    .option(RakChannelOption.RAK_PROTOCOL_VERSION, ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION)
                    .option(RakChannelOption.RAK_CLIENT_BEDROCK_PROTOCOL_VERSION, bedrockProtocolVersion)
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

                            channel.pipeline().addBefore(VLPipeline.VIABEDROCK_FRAME_ENCAPSULATION_HANDLER_NAME, "viabedrock-frame-converter", new MessageToMessageCodec<RakMessage, org.cloudburstmc.netty.channel.raknet.packet.RakMessage>() {
                                @Override
                                protected void encode(ChannelHandlerContext channelHandlerContext, org.cloudburstmc.netty.channel.raknet.packet.RakMessage rakMessage, List<Object> list) {
                                    list.add(new RakMessage(
                                            rakMessage.content().retain(),
                                            RakReliability.valueOf(rakMessage.reliability().name()),
                                            RakPriority.valueOf(rakMessage.priority().name()),
                                            rakMessage.channel()
                                    ));
                                }

                                @Override
                                protected void decode(ChannelHandlerContext channelHandlerContext, RakMessage rakMessage, List<Object> list) {
                                    list.add(new org.cloudburstmc.netty.channel.raknet.packet.RakMessage(
                                            rakMessage.content().retain(),
                                            org.cloudburstmc.netty.channel.raknet.RakReliability.valueOf(rakMessage.reliability().name()),
                                            org.cloudburstmc.netty.channel.raknet.RakPriority.valueOf(rakMessage.priority().name()),
                                            rakMessage.channel()
                                    ));
                                }
                            });
                            channel.pipeline().addBefore(VLPipeline.VIABEDROCK_PACKET_ENCAPSULATION_HANDLER_NAME, "viaproxy-raknetproviders-packet-buffer", new ChannelDuplexHandler() {

                                private boolean sentRequestNetworkSettings = false;
                                private final List<byte[]> bufferedPackets = new ArrayList<>();

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg instanceof ByteBuf byteBuf && !this.sentRequestNetworkSettings) {
                                        final byte[] data = ByteBufUtil.getBytes(byteBuf);
                                        byteBuf.release();
                                        this.bufferedPackets.add(data);
                                        if (this.bufferedPackets.size() > 1000) {
                                            throw new IllegalStateException("Too many packets buffered");
                                        }
                                    } else {
                                        super.channelRead(ctx, msg);
                                    }
                                }

                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    if (msg instanceof ByteBuf byteBuf) {
                                        byteBuf.markReaderIndex();
                                        final int packetId = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(byteBuf) & 1023;
                                        byteBuf.resetReaderIndex();

                                        if (packetId == MinecraftPacketIds.RequestNetworkSettings.getValue()) {
                                            this.sentRequestNetworkSettings = true;
                                            byteBuf.release();
                                            for (byte[] bufferedPacket : this.bufferedPackets) {
                                                ctx.fireChannelRead(Unpooled.wrappedBuffer(bufferedPacket));
                                            }
                                            ctx.pipeline().remove(this);
                                        } else {
                                            super.write(ctx, msg, promise);
                                        }
                                    } else {
                                        super.write(ctx, msg, promise);
                                    }
                                }

                            });
                        }

                    });

            this.channelFuture = bootstrap.register().syncUninterruptibly();

        /*if (this.getChannel().config().setOption(RakChannelOption.RAK_IP_DONT_FRAGMENT, true)) {
            this.getChannel().config().setOption(RakChannelOption.RAK_MTU_SIZES, new Integer[]{1492, 1200, 576});
        }*/
        } else {
            super.initialize(channelType, bootstrap);
        }
    }

}
