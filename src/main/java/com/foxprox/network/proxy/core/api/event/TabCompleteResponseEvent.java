package com.foxprox.network.proxy.core.api.event;

import java.util.List;

import com.foxprox.network.proxy.core.api.connection.Connection;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event called when a backend server sends a response to a player asking to
 * tab-complete a chat message or command. Note that this is not called when
 * BungeeCord or a plugin responds to a tab-complete request. Use
 * {@link TabCompleteEvent} for that.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteResponseEvent extends TargetedEvent implements Cancellable
{

    /**
     * Whether the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Mutable list of suggestions sent back to the player. If this list is
     * empty, an empty list is sent back to the client.
     */
    private final List<String> suggestions;

    public TabCompleteResponseEvent(Connection sender, Connection receiver, List<String> suggestions)
    {
        super( sender, receiver );
        this.suggestions = suggestions;
    }
}
