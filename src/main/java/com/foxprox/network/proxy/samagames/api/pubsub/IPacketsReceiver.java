package com.foxprox.network.proxy.samagames.api.pubsub;

public interface IPacketsReceiver
{
    /**
     * Fired when a Redis PubSub message is received
     *
     * @param channel PubSub message's channel
     * @param packet PubSub message's content
     */
    void receive(String channel, String packet);
}