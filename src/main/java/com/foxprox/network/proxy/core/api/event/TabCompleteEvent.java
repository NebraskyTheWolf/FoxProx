package com.foxprox.network.proxy.core.api.event;

import java.util.List;

import com.foxprox.network.proxy.core.api.connection.Connection;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event called when a player uses tab completion.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * The message the player has already entered.
     */
    private final String cursor;
    /**
     * The suggestions that will be sent to the client. This list is mutable. If
     * this list is empty, the request will be forwarded to the server.
     */
    private final List<String> suggestions;

    public TabCompleteEvent(Connection sender, Connection receiver, String cursor, List<String> suggestions)
    {
        super( sender, receiver );
        this.cursor = cursor;
        this.suggestions = suggestions;
    }
}
