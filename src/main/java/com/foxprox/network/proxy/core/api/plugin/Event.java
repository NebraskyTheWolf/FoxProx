package com.foxprox.network.proxy.core.api.plugin;

import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.samagames.databases.impl.PubSubAPI;
import net.samagames.persistanceapi.GameServiceManager;
import redis.clients.jedis.Jedis;

/**
 * Dummy class which all callable events must extend.
 */
public abstract class Event
{

    /**
     * Method called after this event has been dispatched to all handlers.
     */
    public void postCall()
    {
    }
    // Waterfall start - Add callEvent() shortcut, borrowed from PaperMC/Paper
    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. otherwise true.
     */
    public final boolean callEvent() {
        FoxServer.getInstance().getPluginManager().callEvent(this);
        if (this instanceof Cancellable) {
            return !((Cancellable) this).isCancelled();
        }
        return true;
    }

    public Jedis getHandles() {
        return FoxServer.getInstance().getDatabaseConnector().getBungeeResource();
    }

    public void killHandles(Jedis handle) {
        handle.close();
    }

    public PubSubAPI getPubSub() {
        return FoxServer.getInstance().getPubSub();
    }

    public GameServiceManager getDatabase() {
        return FoxServer.getInstance().getGameServiceManager();
    }

    // Waterfall end
}
