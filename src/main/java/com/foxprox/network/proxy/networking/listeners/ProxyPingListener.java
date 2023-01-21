package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.event.ProxyPingEvent;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.ServerPing;
import com.foxprox.network.proxy.core.event.EventHandler;

public class ProxyPingListener implements Listener {
    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        int online = ping.getPlayers().getOnline();

        ping.setPlayers(new ServerPing.Players(FoxServer.getInstance().getBungeeConfig().getMaxPlayers(), online, null));
        ping.setDescription(FoxServer.getInstance().getBungeeConfig().getMotd());

        event.setResponse(ping);
    }
}
