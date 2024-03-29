package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.PendingConnection;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import com.foxprox.network.proxy.core.api.Callback;
import com.foxprox.network.proxy.core.api.chat.BaseComponent;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.networking.connection.LoginResult; // Waterfall: Parse LoginResult object to new constructor of LoginEvent

/**
 * Event called to represent a player logging in.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LoginEvent extends AsyncEvent<LoginEvent> implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    @Setter(AccessLevel.NONE)
    private BaseComponent[] cancelReasonComponents;

    // Waterfall start - adding the LoginResult variable to provide access to it, when calling the login event
    /**
     * The player's login result containing his textures
     */
    private LoginResult loginResult;
    // Waterfall end

    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;

    public LoginEvent(PendingConnection connection, Callback<LoginEvent> done)
    {
        super( done );
        this.connection = connection;
    }

    // Waterfall start - adding new constructor for LoginResult
    public LoginEvent(PendingConnection connection, Callback<LoginEvent> done, LoginResult loginResult)
    {
        super( done );
        this.connection = connection;
        this.loginResult = loginResult;
    }
    // Waterfall end

    /**
     * @return reason to be displayed
     * @deprecated Use component methods instead.
     */
    @Deprecated
    public String getCancelReason()
    {
        return BaseComponent.toLegacyText( getCancelReasonComponents() );
    }

    /**
     * @param cancelReason reason to be displayed
     * @deprecated Use
     * {@link #setCancelReason(BaseComponent...)}
     * instead.
     */
    @Deprecated
    public void setCancelReason(String cancelReason)
    {
        setCancelReason( TextComponent.fromLegacyText( cancelReason ) );
    }

    public void setCancelReason(BaseComponent... cancelReason)
    {
        this.cancelReasonComponents = cancelReason;
    }
}
