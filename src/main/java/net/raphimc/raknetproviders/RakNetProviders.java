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
package net.raphimc.raknetproviders;

import net.lenni0451.commons.swing.GBC;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.reflect.Objects;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.raknetproviders.b23r0_rust_raknet.B23R0RustRakNet;
import net.raphimc.raknetproviders.b23r0_rust_raknet.B23R0RustRakNetBedrockProxyConnection;
import net.raphimc.raknetproviders.extremeheat_fb_raknet.ExtremeheatFbRakNet;
import net.raphimc.raknetproviders.extremeheat_fb_raknet.ExtremeheatFbRakNetBedrockProxyConnection;
import net.raphimc.raknetproviders.relativitymc_netty_raknet.RelativityMcNettyRakNetBedrockProxyConnection;
import net.raphimc.raknetproviders.sandertv_go_raknet.SanderTvGoRakNet;
import net.raphimc.raknetproviders.sandertv_go_raknet.SanderTvGoRakNetBedrockProxyConnection;
import net.raphimc.raknetproviders.whirvis_jraknet.WhirvisJRakNetBedrockProxyConnection;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.plugins.ViaProxyPlugin;
import net.raphimc.viaproxy.plugins.events.ProxySessionCreationEvent;
import net.raphimc.viaproxy.plugins.events.ViaProxyLoadedEvent;
import net.raphimc.viaproxy.proxy.session.BedrockProxyConnection;
import net.raphimc.viaproxy.ui.ViaProxyWindow;
import net.raphimc.viaproxy.ui.events.UICloseEvent;
import net.raphimc.viaproxy.util.logging.Logger;

import javax.swing.*;
import java.awt.*;

import static net.raphimc.viaproxy.ui.ViaProxyWindow.BODY_BLOCK_PADDING;
import static net.raphimc.viaproxy.ui.ViaProxyWindow.BORDER_PADDING;

public class RakNetProviders extends ViaProxyPlugin {

    private JComboBox<RakNetBackend> rakNetBackend;

    @Override
    public void onEnable() {
        ViaProxy.EVENT_MANAGER.register(this);

        if (ExtremeheatFbRakNet.isLoaded()) {
            ExtremeheatFbRakNet.INSTANCE.RN_SetRakNetProtocolVersion(ProtocolConstants.BEDROCK_RAKNET_PROTOCOL_VERSION);
        }
    }

    @EventHandler
    public void onViaProxyLoaded(final ViaProxyLoadedEvent event) {
        if (ViaProxy.getViaProxyWindow() == null) {
            Logger.LOGGER.warn("ViaProxy UI is not available, RakNetProviders will do nothing!");
            return;
        }
        final ViaProxyWindow ui = ViaProxy.getViaProxyWindow();

        ui.eventManager.register(() -> {
            ViaProxy.getSaveManager().uiSave.put("rakNetBackend", String.valueOf(this.rakNetBackend.getSelectedIndex()));
        }, UICloseEvent.class);

        final JPanel advancedTabPanel = RStream.of(ui.advancedTab).withSuper().fields().by("contentPane").get();
        final JPanel body = (JPanel) advancedTabPanel.getComponent(0);

        {
            final JPanel rakNetBackendPanel = new JPanel(new GridBagLayout());

            final JLabel rakNetBackendLabel = new JLabel("RakNet Backend:");
            GBC.create(rakNetBackendPanel).grid(0, 0).weightx(1).fill(GridBagConstraints.HORIZONTAL).add(rakNetBackendLabel);

            this.rakNetBackend = new JComboBox<>(RakNetBackend.values());
            this.rakNetBackend.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof RakNetBackend backend) {
                        value = backend.getDisplayName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            ViaProxy.getSaveManager().uiSave.loadComboBox("rakNetBackend", this.rakNetBackend);
            GBC.create(rakNetBackendPanel).grid(0, 1).weightx(1).fill(GridBagConstraints.HORIZONTAL).add(this.rakNetBackend);

            final JPanel swingFixPanel = new JPanel(new BorderLayout());
            swingFixPanel.add(rakNetBackendPanel, BorderLayout.NORTH);
            GBC.create(body).grid(0, 5).insets(BODY_BLOCK_PADDING, BORDER_PADDING, 0, BORDER_PADDING).height(4).fill(GBC.HORIZONTAL).add(swingFixPanel);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @EventHandler
    public void onProxySessionCreation(final ProxySessionCreationEvent event) {
        if (event.getProxySession() instanceof BedrockProxyConnection bedrockProxyConnection && this.rakNetBackend.getSelectedItem() instanceof RakNetBackend backend) {
            switch (backend) {
                case EXTREMEHEAT_FB_RAKNET -> {
                    if (ExtremeheatFbRakNet.isLoaded()) {
                        event.setProxySession(new ExtremeheatFbRakNetBedrockProxyConnection(Objects.cast(bedrockProxyConnection, ExtremeheatFbRakNetBedrockProxyConnection.class)));
                    } else {
                        Logger.LOGGER.warn("EXTREMEHEAT_FB_RAKNET is not supported on this system, falling back to CLOUDBURST_NETWORK");
                    }
                }
                case CLOUDBURSTMC_NETWORK -> {
                    // default implementation
                }
                case RELATIVITYMC_NETTY_RAKNET -> {
                    event.setProxySession(new RelativityMcNettyRakNetBedrockProxyConnection(Objects.cast(bedrockProxyConnection, RelativityMcNettyRakNetBedrockProxyConnection.class)));
                }
                case WHIRVIS_JRAKNET -> {
                    event.setProxySession(new WhirvisJRakNetBedrockProxyConnection(Objects.cast(bedrockProxyConnection, WhirvisJRakNetBedrockProxyConnection.class)));
                }
                case SANDERTV_GO_RAKNET -> {
                    if (SanderTvGoRakNet.isLoaded()) {
                        event.setProxySession(new SanderTvGoRakNetBedrockProxyConnection(Objects.cast(bedrockProxyConnection, SanderTvGoRakNetBedrockProxyConnection.class)));
                    } else {
                        Logger.LOGGER.warn("SANDERTV_GO_RAKNET is not supported on this system, falling back to CLOUDBURST_NETWORK");
                    }
                }
                case B23R0_RUST_RAKNET -> {
                    if (B23R0RustRakNet.isLoaded()) {
                        event.setProxySession(new B23R0RustRakNetBedrockProxyConnection(Objects.cast(bedrockProxyConnection, B23R0RustRakNetBedrockProxyConnection.class)));
                    } else {
                        Logger.LOGGER.warn("B23R0_RUST_RAKNET is not supported on this system, falling back to CLOUDBURST_NETWORK");
                    }
                }
            }
        }
    }

}
