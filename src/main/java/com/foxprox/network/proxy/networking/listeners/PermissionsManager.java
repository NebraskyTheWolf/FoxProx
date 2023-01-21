package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.event.PlayerDisconnectEvent;
import com.foxprox.network.proxy.core.api.event.ServerConnectedEvent;
import com.foxprox.network.proxy.core.api.permissions.EntityPermissions;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;
import net.samagames.persistanceapi.beans.permissions.PlayerPermissionsBean;

import java.util.UUID;

public class PermissionsManager implements Listener {
    @EventHandler
    public void onJoin(ServerConnectedEvent event) throws Exception {
        PlayerPermissionsBean permissions = FoxServer.getInstance()
                .getGameServiceManager()
                .getAllPlayerPermissions(event.getPlayer()
                        .handledPlayer()
                );
        event.getPlayer().setPermissions(new EntityPermissions(
                event.getPlayer().getUniqueId(),
                permissions.getApiPermissions(),
                permissions.getBukkitPermissions(),
                permissions.getBungeeRedisPermissions(),
                permissions.getModerationPermissions(),
                permissions.getProxiesPermissions(),
                permissions.getStaffPermissions()
        ));
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        event.getPlayer().resetPermissions();
    }

    public EntityPermissions getPlayerPermission(UUID player) {
        return FoxServer.getInstance().getPlayer(player).getPermission();
    }
}
