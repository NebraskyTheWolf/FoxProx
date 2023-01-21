package com.foxprox.network.proxy.core.api.event;

import java.net.SocketAddress;

import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event called to represent an initial client connection.
 * <br>
 * Note: This event is called at an early stage of every connection, handling
 * should be <b>fast</b>.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ClientConnectEvent extends Event implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Remote address of connection.
     */
    private final SocketAddress socketAddress;
    /**
     * Listener that accepted the connection.
     */
    private final ListenerInfo listener;
}
