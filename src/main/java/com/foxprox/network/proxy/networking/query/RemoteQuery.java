package com.foxprox.network.proxy.networking.query;

import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import com.foxprox.network.proxy.core.api.FoxServer;

@RequiredArgsConstructor
public class RemoteQuery
{

    private final FoxServer bungee;
    private final ListenerInfo listener;

    public void start(Class<? extends Channel> channel, InetSocketAddress address, EventLoopGroup eventLoop, ChannelFutureListener future)
    {
        new Bootstrap()
                .channel( channel )
                .group( eventLoop )
                .handler( new QueryHandler( bungee, listener ) )
                .localAddress( address )
                .bind().addListener( future );
    }
}
