package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.event.PostLoginEvent;
import com.foxprox.network.proxy.core.api.event.ServerConnectEvent;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.samagames.api.channels.JsonModMessage;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.FetchLobby;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;
import net.samagames.persistanceapi.beans.players.PlayerBean;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(ServerConnectEvent event) throws Exception {
        PlayerBean player = event.getPlayer().handledPlayer();
        if (player != null) {
            event.getPlayer().updateHandledPlayer();

            if (event.getPlayer().isBanned())
                event.getPlayer().disconnect(new TextComponent(FoxProx.PROXY_TAG + ChatColor.YELLOW + "You are " + ChatColor.RED + "PERMANENTLY " + ChatColor.YELLOW + "banned for " + ChatColor.RED + "Cheat/Bypass"));
        } else {
           event.getPlayer().createHandledPlayer();
        }

        if (FoxServer.getInstance().getLobbyManager().isServerAvailable()) {
            FetchLobby lobby = FoxServer.getInstance().getLobbyManager();
            event.getPlayer().connect(lobby.fetchRandomHub());
        } else {
            event.getPlayer().disconnect(new TextComponent(FoxProx.PROXY_TAG + ChatColor.RED + "Impossible to connect on the server. The proxy is restarting..."));
        }
    }

    @EventHandler
    public void onChangeServer(ServerConnectEvent event) throws IOException {
        event.getHandles().set("lastserver:" + event.getPlayer().getUniqueId().toString(), event.getTarget().getName());
        event.getHandles().set("currentserver:" + event.getPlayer().getUniqueId().toString(), event.getTarget().getName());

        if (!event.getTarget().isHydroManaged()) {
            FoxServer.getInstance().sendModerationMessage(ModChannel.INFORMATION, new JsonModMessage()
                    .setSender("FoxProx")
                    .setMessage(String.format("%s connected on a non-managed server.", event.getPlayer().getName()))
            );
        }
    }

    @EventHandler
    public void onJoins(PostLoginEvent event) {
        if (event.getPlayer().isForgeUser()) {
            event.getPlayer()
                    .getModList()
                    .forEach((modId, version) -> event.getHandles().sadd(
                            String.format("forge:%s/mods", event.getPlayer().getUniqueId().toString()),
                            String.format("%s:%s", modId, version)
                    ));
            event.getHandles().close();
        }
    }
}
