package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.plugin.Event;
import com.foxprox.network.proxy.core.api.CommandSender;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Called when somebody reloads BungeeCord
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProxyReloadEvent extends Event
{

    /**
     * Creator of the action.
     */
    private final CommandSender sender;
}
