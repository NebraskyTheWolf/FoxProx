package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.Connection;
import com.foxprox.network.proxy.core.api.plugin.Cancellable;
import com.foxprox.network.proxy.core.api.plugin.PluginManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;

/**
 * Event called when a player sends a message to a server.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChatEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Text contained in this chat.
     */
    private String message;

    public ChatEvent(Connection sender, Connection receiver, String message)
    {
        super( sender, receiver );
        this.message = message;
    }

    /**
     * Checks whether this message is valid as a command
     *
     * @return if this message is a command
     */
    public boolean isCommand()
    {
        return message.length() > 0 && message.charAt( 0 ) == '/';
    }

    /**
     * Checks whether this message is run on this proxy server.
     *
     * @return if this command runs on the proxy
     * @see PluginManager#isExecutableCommand(java.lang.String,
     * CommandSender)
     */
    public boolean isProxyCommand()
    {
        if ( !isCommand() )
        {
            return false;
        }

        int index = message.indexOf( " " );
        String commandName = ( index == -1 ) ? message.substring( 1 ) : message.substring( 1, index );
        CommandSender sender = ( getSender() instanceof CommandSender ) ? (CommandSender) getSender() : null;

        return FoxServer.getInstance().getPluginManager().isExecutableCommand( commandName, sender );
    }
}
