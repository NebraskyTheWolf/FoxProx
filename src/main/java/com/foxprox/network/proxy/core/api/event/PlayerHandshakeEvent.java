package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.PendingConnection;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import com.foxprox.network.proxy.core.api.plugin.Event;
import com.foxprox.network.proxy.networking.protocol.packet.Handshake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Event called to represent a player first making their presence and username
 * known.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
// FlameCord - Implement cancellable
public class PlayerHandshakeEvent extends Event implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled = false;

    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;
    /**
     * The handshake.
     */
    private final Handshake handshake;

    public PlayerHandshakeEvent(PendingConnection connection, Handshake handshake)
    {
        this.connection = connection;
        this.handshake = handshake;
    }
}
