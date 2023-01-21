package com.foxprox.network.proxy.core.netty;

import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.event.ClientConnectEvent;
import com.foxprox.network.proxy.lib.event.ConnectionInitEvent;
import com.foxprox.network.proxy.networking.connection.InitialHandler;
import com.google.common.base.Preconditions;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.PlatformDependent;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.Util;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.networking.protocol.KickStringWriter;
import com.foxprox.network.proxy.networking.protocol.LegacyDecoder;
import com.foxprox.network.proxy.networking.protocol.MinecraftDecoder;
import com.foxprox.network.proxy.networking.protocol.MinecraftEncoder;
import com.foxprox.network.proxy.networking.protocol.Protocol;
import com.foxprox.network.proxy.networking.protocol.Varint21FrameDecoder;
import com.foxprox.network.proxy.networking.protocol.Varint21LengthFieldPrepender;

public class PipelineUtils
{

    public static final AttributeKey<ListenerInfo> LISTENER = AttributeKey.newInstance( "ListerInfo" );
    public static final ChannelInitializer<Channel> SERVER_CHILD = new ChannelInitializer<Channel>()
    {
        @Override
        protected void initChannel(Channel ch) throws Exception
        {
            SocketAddress remoteAddress = ( ch.remoteAddress() == null ) ? ch.parent().localAddress() : ch.remoteAddress();

            if ( FoxProx.getInstance().getConnectionThrottle() != null && FoxProx.getInstance().getConnectionThrottle().throttle( remoteAddress ) )
            {
                ch.close();
                return;
            }
            ListenerInfo listener = ch.attr( LISTENER ).get();

            if ( FoxProx.getInstance().getPluginManager().callEvent( new ClientConnectEvent( remoteAddress, listener ) ).isCancelled() )
            {
                ch.close();
                return;
            }

            ConnectionInitEvent connectionInitEvent = new ConnectionInitEvent(ch.remoteAddress(), listener, (result, throwable) -> { // Waterfall

            if (result.isCancelled()) {
                ch.close();
                return;
            }


            try {
            BASE.initChannel( ch );
            } catch (Exception e) {
                e.printStackTrace();
                ch.close();
                return;
            }
            ch.pipeline().addBefore( FRAME_DECODER, LEGACY_DECODER, new LegacyDecoder() );
            ch.pipeline().addAfter( FRAME_DECODER, PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, true, FoxServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addAfter( FRAME_PREPENDER, PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, true, FoxServer.getInstance().getProtocolVersion() ) );
            ch.pipeline().addBefore( FRAME_PREPENDER, LEGACY_KICKER, legacyKicker );
            ch.pipeline().get( HandlerBoss.class ).setHandler( new InitialHandler( FoxProx.getInstance(), listener ) );

            if ( listener.isProxyProtocol() )
            {
                ch.pipeline().addFirst( new HAProxyMessageDecoder() );
            }
            }); // Waterfall

            FoxProx.getInstance().getPluginManager().callEvent(connectionInitEvent);
        }

        // FlameCord - Close on exception caught
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            cause.printStackTrace();

            ctx.close();
        }
    };
    public static final Base BASE = new Base();
    private static final KickStringWriter legacyKicker = new KickStringWriter();
    private static final Varint21LengthFieldPrepender framePrepender = new Varint21LengthFieldPrepender();
    public static final String TIMEOUT_HANDLER = "timeout";
    public static final String PACKET_DECODER = "packet-decoder";
    public static final String PACKET_ENCODER = "packet-encoder";
    public static final String BOSS_HANDLER = "inbound-boss";
    public static final String ENCRYPT_HANDLER = "encrypt";
    public static final String DECRYPT_HANDLER = "decrypt";
    public static final String FRAME_DECODER = "frame-decoder";
    public static final String FRAME_PREPENDER = "frame-prepender";
    public static final String LEGACY_DECODER = "legacy-decoder";
    public static final String LEGACY_KICKER = "legacy-kick";

    private static boolean epoll;

    static
    {
        if ( !PlatformDependent.isWindows() && Boolean.parseBoolean( System.getProperty( "bungee.epoll", "true" ) ) )
        {
            FoxServer.getInstance().getLogger().info( "Not on Windows, attempting to use enhanced EpollEventLoop" );

            if ( epoll = Epoll.isAvailable() )
            {
                FoxServer.getInstance().getLogger().info( "Epoll is working, utilising it!" );
            } else
            {
                FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "Epoll is not working, falling back to NIO: {0}", Util.exception( Epoll.unavailabilityCause() ) );
            }
        }
    }

    public static EventLoopGroup newEventLoopGroup(int threads, ThreadFactory factory)
    {
        return epoll ? new EpollEventLoopGroup( threads, factory ) : new NioEventLoopGroup( threads, factory );
    }

    public static Class<? extends ServerChannel> getServerChannel(SocketAddress address)
    {
        if ( address instanceof DomainSocketAddress )
        {
            Preconditions.checkState( epoll, "Epoll required to have UNIX sockets" );

            return EpollServerDomainSocketChannel.class;
        }

        return epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> getChannel(SocketAddress address)
    {
        if ( address instanceof DomainSocketAddress )
        {
            Preconditions.checkState( epoll, "Epoll required to have UNIX sockets" );

            return EpollDomainSocketChannel.class;
        }

        return epoll ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends DatagramChannel> getDatagramChannel()
    {
        return epoll ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    private static final int LOW_MARK = Integer.getInteger( "net.md_5.bungee.low_mark", 2 << 18 ); // 0.5 mb
    private static final int HIGH_MARK = Integer.getInteger( "net.md_5.bungee.high_mark", 2 << 20 ); // 2 mb
    private static final WriteBufferWaterMark MARK = new WriteBufferWaterMark( LOW_MARK, HIGH_MARK );

    public static final class Base extends ChannelInitializer<Channel>
    {

        @Override
        public void initChannel(Channel ch) throws Exception
        {
            try
            {
                ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
            } catch ( ChannelException ex )
            {
                // IP_TOS is not supported (Windows XP / Windows Server 2003)
            }
            ch.config().setOption( ChannelOption.TCP_NODELAY, true );
            // FlameCord - TCP Fast Open
            ch.config().setOption( ChannelOption.TCP_FASTOPEN, 1);
            ch.config().setAllocator( PooledByteBufAllocator.DEFAULT );
            ch.config().setWriteBufferWaterMark( MARK );

            ch.pipeline().addLast( FRAME_DECODER, new Varint21FrameDecoder() );
            ch.pipeline().addLast( TIMEOUT_HANDLER, new ReadTimeoutHandler( FoxProx.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS ) );
            ch.pipeline().addLast( FRAME_PREPENDER, framePrepender );

            ch.pipeline().addLast( BOSS_HANDLER, new HandlerBoss() );
        }

        // FlameCord - Close on exception caught
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            cause.printStackTrace();

            ctx.close();
        }
    }
}
