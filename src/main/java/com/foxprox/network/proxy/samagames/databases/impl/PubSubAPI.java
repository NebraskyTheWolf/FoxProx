package com.foxprox.network.proxy.samagames.databases.impl;

import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.samagames.api.pubsub.*;
import redis.clients.jedis.Jedis;

public class PubSubAPI implements IPubSubAPI
{

    private final Subscriber subscriberPattern;
    private final Subscriber subscriberChannel;

    private final Sender sender;
    private final FoxServer api;

    boolean working = true;

    private final Thread senderThread;
    private Thread patternThread;
    private Thread channelThread;

    // Avoid to init Threads before the subclass constructor is started (Fix possible atomicity violation)
    public PubSubAPI(FoxServer api)
    {
        this.api = api;
        subscriberPattern = new Subscriber();
        subscriberChannel = new Subscriber();

        sender = new Sender(api);
        senderThread = new Thread(sender, "SenderThread");
        senderThread.start();

        startThread();
    }

    private void startThread()
    {
        patternThread = new Thread(() -> {
            while (working)
            {
                Jedis jedis = api.getRedisConnection();
                try
                {
                    String[] patternsSuscribed = subscriberPattern.getPatternsSuscribed();
                    if(patternsSuscribed.length > 0)
                        jedis.psubscribe(subscriberPattern, patternsSuscribed);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                jedis.close();
            }
        });
        patternThread.start();

        channelThread = new Thread(() -> {
            while (working)
            {
                Jedis jedis = api.getRedisConnection();
                try
                {
                    String[] channelsSuscribed = subscriberChannel.getChannelsSuscribed();
                    if (channelsSuscribed.length > 0)
                        jedis.subscribe(subscriberChannel, channelsSuscribed);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                jedis.close();
            }
        });
        channelThread.start();
    }

    @Override
    public void subscribe(String channel, IPacketsReceiver receiver)
    {
        subscriberChannel.registerReceiver(channel, receiver);
        if(subscriberChannel.isSubscribed())
            subscriberChannel.unsubscribe();
    }

    @Override
    public void subscribe(String pattern, IPatternReceiver receiver)
    {
        subscriberPattern.registerPattern(pattern, receiver);
        if(subscriberPattern.isSubscribed())
            subscriberPattern.punsubscribe();
    }

    @Override
    public void send(String channel, String message)
    {
        sender.publish(new PendingMessage(channel, message));
    }

    @Override
    public void send(PendingMessage message)
    {
        sender.publish(message);
    }

    @Override
    public ISender getSender()
    {
        return sender;
    }

    public void disable()
    {
        working = false;
        subscriberChannel.unsubscribe();
        subscriberPattern.punsubscribe();
        try
        {
            Thread.sleep(500);
        } catch (Exception ignored)
        {
        }

        senderThread.stop();
        patternThread.stop();
        channelThread.stop();
    }
}