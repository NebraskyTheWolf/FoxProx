package com.foxprox.network.proxy;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.Callback;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.ServerPing;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.connection.Server;
import com.foxprox.network.proxy.networking.connection.PingHandler;
import com.foxprox.network.proxy.core.netty.HandlerBoss;
import com.foxprox.network.proxy.core.netty.PipelineUtils;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@ToString(of =
{
    "name", "socketAddress", "restricted"
})
// CHECKSTYLE:ON
public class FoxServerInfo implements ServerInfo
{

    @Getter
    private final String name;

    private boolean isManaged;

    @Getter
    private final SocketAddress socketAddress;
    private final Collection<FoxPlayer> players = new ArrayList<>();
    @Getter
    private final String motd;
    @Getter
    private final boolean restricted;
    @Getter
    private final Queue<DefinedPacket> packetQueue = new LinkedList<>();

    @Synchronized("players")
    public void addPlayer(FoxPlayer player)
    {
        players.add( player );
    }

    @Synchronized("players")
    public void removePlayer(FoxPlayer player)
    {
        players.remove( player );
    }

    @Synchronized("players")
    @Override
    public Collection<FoxPlayer> getPlayers()
    {
        return Collections.unmodifiableCollection( new HashSet<>( players ) );
    }

    @Override
    public String getPermission()
    {
        return "bungeecord.server." + name;
    }

    @Override
    public boolean canAccess(CommandSender player)
    {
        return FoxServer.getInstance()
                .getPlayerPermissions(((FoxPlayer) player).getUniqueId())
                .getStaffPermissionsBean()
                .isNetworkAdmin();
    }

    @Override
    public boolean equals(Object obj)
    {
        return ( obj instanceof ServerInfo ) && Objects.equals( getAddress(), ( (ServerInfo) obj ).getAddress() );
    }

    @Override
    public int hashCode()
    {
        return socketAddress.hashCode();
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        sendData( channel, data, true );
    }

    // TODO: Don't like this method
    @Override
    public boolean sendData(String channel, byte[] data, boolean queue)
    {
        Preconditions.checkNotNull( channel, "channel" );
        Preconditions.checkNotNull( data, "data" );

        Server server;

        synchronized (players) {
            server = players.isEmpty() ? null : players.iterator().next().getServer();
        }

        if (server != null) {
            server.sendData(channel, data);
            return true;
        } else if (queue) {
            synchronized (packetQueue) {
                packetQueue.add(new PluginMessage(channel, data, false));
            }
        }

        return false;
    }

    private long lastPing;
    private ServerPing cachedPing;

    public void cachePing(ServerPing serverPing)
    {
        if ( FoxServer.getInstance().getConfig().getRemotePingCache() > 0 )
        {
            this.cachedPing = serverPing;
            this.lastPing = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isHydroManaged() {
        return this.getName().contains("_");
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) socketAddress;
    }

    @Override
    public void ping(final Callback<ServerPing> callback)
    {
        ping( callback, FoxServer.getInstance().getProtocolVersion() );
    }

    public void ping(final Callback<ServerPing> callback, final int protocolVersion)
    {
        Preconditions.checkNotNull( callback, "callback" );

        int pingCache = FoxServer.getInstance().getConfig().getRemotePingCache();
        if ( pingCache > 0 && cachedPing != null && ( System.currentTimeMillis() - lastPing ) > pingCache )
        {
            cachedPing = null;
        }

        if ( cachedPing != null )
        {
            callback.done( cachedPing, null );
            return;
        }

        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( future.isSuccess() )
                {
                    future.channel().pipeline().get( HandlerBoss.class ).setHandler( new PingHandler( FoxServerInfo.this, callback, protocolVersion ) );
                } else
                {
                    callback.done( null, future.cause() );
                }
            }
        };
        new Bootstrap()
                .channel( PipelineUtils.getChannel( socketAddress ) )
                .group( FoxProx.getInstance().workerEventLoopGroup )
                .handler( PipelineUtils.BASE )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, FoxProx.getInstance().getConfig().getRemotePingTimeout() )
                .remoteAddress( socketAddress )
                .connect()
                .addListener( listener );
    }

    public void setHydroManaged(boolean managed) {
        this.isManaged = managed;
    }
}
