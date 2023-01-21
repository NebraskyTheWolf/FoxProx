package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.Connection;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An event which occurs in the communication between two nodes.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class TargetedEvent extends Event
{

    /**
     * Creator of the action.
     */
    private final Connection sender;
    /**
     * Receiver of the action.
     */
    private final Connection receiver;
}
