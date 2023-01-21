package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Called when a player has left the proxy, it is not safe to call any methods
 * that perform an action on the passed player instance.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerDisconnectEvent extends Event
{

    /**
     * Player disconnecting.
     */
    private final FoxPlayer player;
}
