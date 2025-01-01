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
package net.raphimc.raknetproviders.b23r0_rust_raknet;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.util.HashMap;
import java.util.Map;

public interface B23R0RustRakNet extends Library {

    B23R0RustRakNet INSTANCE = loadNative();

    private static B23R0RustRakNet loadNative() {
        try {
            final Map<String, Object> options = new HashMap<>();
            options.put(Library.OPTION_STRING_ENCODING, "UTF-8");
            return Native.load("rust-raknet", B23R0RustRakNet.class, options);
        } catch (Throwable ignored) {
        }
        return null;
    }

    static boolean isLoaded() {
        return INSTANCE != null;
    }

    String connect(final String address, final int connectTimeout, final byte raknetVersion, final PointerByReference connectionPointer);

    void begin_receive_packet(final Pointer connection, final int readTimeout, final Callback readCallback);

    String send_packet(final Pointer connection, final byte[] data, final int length);

    String disconnect(final Pointer connection);

}
