package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Called when a player has changed servers.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerSwitchEvent extends Event
{

    /**
     * Player whom the server is for.
     */
    private final FoxPlayer player;
    /**
     * Server the player is switch from. May be null if initial proxy
     * connection.
     */
    private final ServerInfo from;
}
