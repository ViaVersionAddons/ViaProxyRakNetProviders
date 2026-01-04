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
package net.raphimc.raknetproviders;

public enum RakNetBackend {

    KASTLE_NETWORK("[Java] Kas-tle/Network (default)"), // https://github.com/Kas-tle/NetworkCompatible
    EXTREMEHEAT_FB_RAKNET("[C++] extremeheat/fb-raknet"), // https://github.com/extremeheat/fb-raknet
    CLOUDBURSTMC_NETWORK("[Java] CloudburstMC/Network"), // https://github.com/CloudburstMC/Network
    SANDERTV_GO_RAKNET("[Go] Sandertv/go-raknet"), // https://github.com/Sandertv/go-raknet
    RELATIVITYMC_NETTY_RAKNET("[Java] RelativityMC/netty-raknet"), // https://github.com/RelativityMC/netty-raknet
    B23R0_RUST_RAKNET("[Rust] b23r0/rust-raknet"), // https://github.com/b23r0/rust-raknet
    WHIRVIS_JRAKNET("[Java] whirvis/JRakNet (bad)"), // https://github.com/telecran-telecrit/JRakNet
    ;

    private final String displayName;

    RakNetBackend(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
