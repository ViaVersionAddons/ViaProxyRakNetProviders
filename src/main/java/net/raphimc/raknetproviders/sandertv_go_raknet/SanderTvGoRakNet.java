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
package net.raphimc.raknetproviders.sandertv_go_raknet;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.HashMap;
import java.util.Map;

public interface SanderTvGoRakNet extends Library {

    SanderTvGoRakNet INSTANCE = loadNative();

    private static SanderTvGoRakNet loadNative() {
        try {
            final Map<String, Object> options = new HashMap<>();
            options.put(Library.OPTION_STRING_ENCODING, "UTF-8");
            return Native.load("go-raknet", SanderTvGoRakNet.class, options);
        } catch (Throwable ignored) {
        }
        return null;
    }

    static boolean isLoaded() {
        return INSTANCE != null;
    }

    String connect(final String address, final long timeout, final PointerByReference connectionPointer);

    String receivePacket(final Pointer connection, final PointerByReference data, final IntByReference length);

    String sendPacket(final Pointer connection, final byte[] data, final int length);

    long getLatency(final Pointer connection);

    String disconnect(final Pointer connection);

}
