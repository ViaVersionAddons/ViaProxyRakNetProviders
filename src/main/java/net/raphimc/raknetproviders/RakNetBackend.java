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
package net.raphimc.raknetproviders;

public enum RakNetBackend {

    GO_RAKNET("Sandertv/go-raknet (best)"), // https://github.com/Sandertv/go-raknet
    NATIVE_RAKNET("Native RakNet"), // https://github.com/extremeheat/fb-raknet
    CLOUDBURSTMC_NETWORK("CloudburstMC/Network (default)"), // https://github.com/CloudburstMC/Network
    RELATIVITYMC_NETTY_RAKNET("RelativityMC/netty-raknet"), // https://github.com/RelativityMC/netty-raknet
    WHIRVIS_JRAKNET("whirvis/JRakNet (bad)"), // https://github.com/telecran-telecrit/JRakNet
    ;

    private final String displayName;

    RakNetBackend(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
