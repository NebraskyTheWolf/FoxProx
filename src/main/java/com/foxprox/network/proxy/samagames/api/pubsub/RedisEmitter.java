package com.foxprox.network.proxy.samagames.api.pubsub;

import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.PluginManager;
import com.foxprox.network.proxy.core.api.FoxServer;

public class RedisEmitter implements IPacketsReceiver {

    public final PluginManager manager = FoxServer.getInstance().getPluginManager();

    @Override
    public void receive(String channel, String packet) {
        FoxServer.getInstance().getPluginManager().eventBus.post(new PubSubMessage(
                        "",
                        channel,
                        packet),
                manager::handleEventException
        );
    }

    public static class PatternEmitter implements IPatternReceiver {
        @Override
        public void receive(String pattern, String channel, String packet) {
            FoxServer.getInstance().getPluginManager().eventBus.post(new PubSubMessage(
                            pattern,
                            channel,
                            packet),
                    FoxServer.getInstance().getPluginManager()::handleEventException
            );
        }
    }
}
