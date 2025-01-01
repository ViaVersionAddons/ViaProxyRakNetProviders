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
package net.raphimc.raknetproviders.extremeheat_fb_raknet;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface ExtremeheatFbRakNet extends Library {

    ExtremeheatFbRakNet INSTANCE = loadNative();

    private static ExtremeheatFbRakNet loadNative() {
        try {
            return Native.load("cpp-raknet", ExtremeheatFbRakNet.class);
        } catch (Throwable ignored) {
        }
        return null;
    }

    static boolean isLoaded() {
        return INSTANCE != null;
    }

    Pointer RN_RakPeerGetInstance();

    void RN_SetRakNetProtocolVersion(final int version);

    void RN_RakPeerDestroyInstance(final Pointer rakPeer);

    int RN_RakPeerStartup(final Pointer rakPeer, final int maxConnections, RN_SocketDescriptor[] socketDescriptors, final int socketDescriptorCount, final int threadPriority);

    int RN_RakPeerConnect(final Pointer rakPeer, final String host, final short remotePort, final String passwordData, final int passwordDataLength, final Pointer publicKey, final int connectionSocketIndex, final int sendConnectionAttemptCount, final int timeBetweenSendConnectionAttemptsMS, final int timeoutTimeMS);

    void RN_RakPeerShutdown(final Pointer rakPeer, final int blockDuration, final int orderingChannel, final int disconnectionNotificationPriority);

    RN_Packet RN_RakPeerReceive(final Pointer rakPeer);

    int RN_RakPeerSend(final Pointer rakPeer, final byte[] data, final int length, final int priority, final int reliability, final byte orderingChannel, final RN_AddressOrGUID.ByValue systemAddress, final boolean broadcast, final int forceReceiptNumber);

    void RN_RakPeerDeallocatePacket(final Pointer rakPeer, final RN_Packet packet);

    @Structure.FieldOrder({"port", "hostAddress", "socketFamily", "remotePortRakNetWasStartedOn_PS3", "chromeInstance", "blockingSocket", "extraSocketOptions"})
    class RN_SocketDescriptor extends Structure {

        public short port;
        public byte[] hostAddress = new byte[32];
        public short socketFamily;
        public short remotePortRakNetWasStartedOn_PS3;
        public int chromeInstance;
        public boolean blockingSocket;
        public int extraSocketOptions;

        public RN_SocketDescriptor() {
        }

        public RN_SocketDescriptor(final short port, final String hostAddress, final short socketFamily, final short remotePortRakNetWasStartedOn_PS3, final int chromeInstance, final boolean blockingSocket, final int extraSocketOptions) {
            this.port = port;
            this.hostAddress = Arrays.copyOf(hostAddress.getBytes(StandardCharsets.US_ASCII), 32);
            this.socketFamily = socketFamily;
            this.remotePortRakNetWasStartedOn_PS3 = remotePortRakNetWasStartedOn_PS3;
            this.chromeInstance = chromeInstance;
            this.blockingSocket = blockingSocket;
            this.extraSocketOptions = extraSocketOptions;
        }

    }

    @Structure.FieldOrder({"padding", "length", "bitSize", "data", "deleteData", "wasGeneratedLocally"})
    class RN_Packet extends Structure {

        public byte[] padding = new byte[40];
        public int length;
        public int bitSize;
        public Pointer data;
        public boolean deleteData;
        public boolean wasGeneratedLocally;

    }

    @Structure.FieldOrder({"guid", "systemIndex", "padding"})
    class RN_AddressOrGUID extends Structure {

        public long guid;
        public short systemIndex;
        public byte[] padding = new byte[20];

        public RN_AddressOrGUID() {
        }

        public RN_AddressOrGUID(final long guid, final short systemIndex) {
            this.guid = guid;
            this.systemIndex = systemIndex;
        }

        public static class ByReference extends RN_AddressOrGUID implements Structure.ByReference {
        }

        public static class ByValue extends RN_AddressOrGUID implements Structure.ByValue {
        }

    }

}
