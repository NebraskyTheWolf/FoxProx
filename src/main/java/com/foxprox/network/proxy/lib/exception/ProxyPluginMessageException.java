package com.foxprox.network.proxy.lib.exception;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.Plugin;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thrown when an incoming plugin message channel throws an exception
 */
public class ProxyPluginMessageException extends ProxyPluginException {

    private final FoxPlayer player;
    private final String channel;
    private final byte[] data;

    public ProxyPluginMessageException(String message, Throwable cause, Plugin responsiblePlugin, FoxPlayer player, String channel, byte[] data) {
        super(message, cause, responsiblePlugin);
        this.player = checkNotNull(player, "player");
        this.channel = checkNotNull(channel, "channel");
        this.data = checkNotNull(data, "data");
    }

    public ProxyPluginMessageException(Throwable cause, Plugin responsiblePlugin, FoxPlayer player, String channel, byte[] data) {
        super(cause, responsiblePlugin);
        this.player = checkNotNull(player, "player");
        this.channel = checkNotNull(channel, "channel");
        this.data = checkNotNull(data, "data");
    }

    protected ProxyPluginMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Plugin responsiblePlugin, FoxPlayer player, String channel, byte[] data) {
        super(message, cause, enableSuppression, writableStackTrace, responsiblePlugin);
        this.player = checkNotNull(player, "player");
        this.channel = checkNotNull(channel, "channel");
        this.data = checkNotNull(data, "data");
    }

    /**
     * Gets the channel to which the error occurred from recieving data from
     *
     * @return exception channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets the data to which the error occurred from
     *
     * @return exception data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the player which the plugin message causing the exception originated from
     *
     * @return exception player
     */
    public FoxPlayer getPlayer() {
        return player;
    }
}
