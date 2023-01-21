package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.google.gson.Gson;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;
import java.util.UUID;

public class NetworkListeners implements Listener {
    @EventHandler
    public void onRequest(PubSubMessage message) {
        if (message.getChannel().contains("apiexec")) {
            String[] args = message.getMessage().split(" ");
            FoxPlayer player = FoxServer.getInstance().getPlayer(UUID.fromString(args[0]));
            switch (message.getChannel()) {
                case "apiexec.connect": {
                    ServerInfo server = FoxServer.getInstance().getServerInfo(args[1]);
                    if (server.isHydroManaged())
                        player.connect(server);
                }
                break;
                case "apiexec.send": {
                    TextComponent component = new Gson().fromJson(args[1], TextComponent.class);
                    if (component != null)
                        player.sendMessage(component);
                }
                break;
                case "apiexec.kick": {
                    TextComponent component = new Gson().fromJson(args[1], TextComponent.class);
                    player.disconnect(component);
                }
                break;
            }
        }
    }
}
