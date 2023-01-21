package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.foxprox.network.proxy.core.api.chat.BaseComponent;
import com.foxprox.network.proxy.core.api.chat.TextComponent;

/**
 * Represents a player getting kicked from a server.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerKickEvent extends Event implements Cancellable
{

    /**
     * Cancelled status.
     */
    private boolean cancelled;
    /**
     * Player being kicked.
     */
    private final FoxPlayer player;
    /**
     * The server the player was kicked from, should be used in preference to
     * {@link FoxPlayer#getServer()}.
     */
    private final ServerInfo kickedFrom;
    /**
     * Kick reason.
     */
    private BaseComponent[] kickReasonComponent;
    /**
     * Server to send player to if this event is cancelled.
     */
    private ServerInfo cancelServer;
    /**
     * State in which the kick occured.
     */
    private State state;
    // Waterfall start
    /**
     * Circumstances which led to the kick.
     */
    private Cause cause;
    // Waterfall end

    public enum State
    {

        CONNECTING, CONNECTED, UNKNOWN;
    }

    // Waterfall start
    public enum Cause
    {

        SERVER, LOST_CONNECTION, EXCEPTION, UNKNOWN;
    }
    // Waterfall end

    @Deprecated
    public ServerKickEvent(FoxPlayer player, BaseComponent[] kickReasonComponent, ServerInfo cancelServer)
    {
        this( player, kickReasonComponent, cancelServer, State.UNKNOWN );
    }

    @Deprecated
    public ServerKickEvent(FoxPlayer player, BaseComponent[] kickReasonComponent, ServerInfo cancelServer, State state)
    {
        this( player, player.getServer().getInfo(), kickReasonComponent, cancelServer, state );
    }

    // Waterfall start
    @Deprecated
    public ServerKickEvent(FoxPlayer player, ServerInfo kickedFrom, BaseComponent[] kickReasonComponent, ServerInfo cancelServer, State state)
    {
        this( player, kickedFrom, kickReasonComponent, cancelServer, state, Cause.UNKNOWN );
    }

    public ServerKickEvent(FoxPlayer player, ServerInfo kickedFrom, BaseComponent[] kickReasonComponent, ServerInfo cancelServer, State state, Cause cause)
    {
        this.player = player;
        this.kickedFrom = kickedFrom;
        this.kickReasonComponent = kickReasonComponent;
        this.cancelServer = cancelServer;
        this.state = state;
        this.cause = cause;
    }
    // Waterfall end

    @Deprecated
    public String getKickReason()
    {
        return BaseComponent.toLegacyText( kickReasonComponent );
    }

    @Deprecated
    public void setKickReason(String reason)
    {
        kickReasonComponent = TextComponent.fromLegacyText( reason );
    }
}
