package com.foxprox.network.proxy.networking.managers;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.networking.connection.InitialHandler;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.Handshake;

public class MerlinHandler {
    public static void handle(InitialHandler handler, Handshake client) {
        if (client.getProtocolVersion() != ProtocolConstants.MERLIN_CLIENT) {
            handler.disconnect(FoxProx.PROXY_TAG + ChatColor.RED + "You must use MERLIN Client to join the server.");
        }
    }
}
