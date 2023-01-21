package com.foxprox.network.proxy.core.netty;

import com.foxprox.network.proxy.networking.connection.CancelSendSignal;
import com.foxprox.network.proxy.networking.connection.InitialHandler;
import com.foxprox.network.proxy.networking.connection.PingHandler;
import com.foxprox.network.proxy.networking.protocol.BadPacketException;
import com.foxprox.network.proxy.networking.protocol.MinecraftDecoder;
import com.foxprox.network.proxy.networking.protocol.OverflowPacketException;
import com.foxprox.network.proxy.networking.protocol.PacketWrapper;
import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.util.QuietException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter
{

    private ChannelWrapper channel;
    private PacketHandler handler;

    public void setHandler(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            channel = new ChannelWrapper( ctx );
            handler.connected( channel );

            // FlameCord - Option to log initialhandler
            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler) )
            {
                FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.INFO, "{0} has connected", handler );
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            channel.markClosed();
            handler.disconnected( channel );

            // FlameCord - Option to log initialhandler
            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) )
            {
                FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.INFO, "{0} has disconnected", handler );
            }
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.writabilityChanged( channel );
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // FlameCord - Return if channel isn't active
        if (!ctx.channel().isActive()) {
            if (msg instanceof PacketWrapper) {
                ((PacketWrapper) msg).trySingleRelease();
            }

            return;
        }

        if ( msg instanceof HAProxyMessage )
        {
            HAProxyMessage proxy = (HAProxyMessage) msg;
            try
            {
                if ( proxy.sourceAddress() != null )
                {
                    InetSocketAddress newAddress = new InetSocketAddress( proxy.sourceAddress(), proxy.sourcePort() );

                    // FlameCord - Option to log haproxy

                    channel.setRemoteAddress( newAddress );
                }
            } finally
            {
                proxy.release();
            }
            return;
        }

        PacketWrapper packet = (PacketWrapper) msg;


        try
        {
            if ( handler != null )
            {
                boolean sendPacket = handler.shouldHandle( packet );
                if ( sendPacket && packet.packet != null )
                {
                    try
                    {
                        packet.packet.handle( handler );
                    } catch ( CancelSendSignal ex )
                    {
                        sendPacket = false;
                    }
                }
                if ( sendPacket )
                {
                    handler.handle( packet );
                }
            }
        } finally
        {
            packet.trySingleRelease();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // Flamecord start - Antibot System
        // Flamecord end - Antibot System

        if ( ctx.channel().isActive() )
        {
            boolean logExceptions = !( handler instanceof PingHandler );

            if ( logExceptions )
            {
                if ( cause instanceof ReadTimeoutException )
                {
                    FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - read timed out", handler );
                } else if ( cause instanceof DecoderException )
                {
                    // Waterfall start
                    // Waterfall end
                    if ( cause instanceof CorruptedFrameException )
                    {
                        FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - corrupted frame: {1}", new Object[]
                        {
                            handler, cause.getMessage()
                        } );
                    } else if ( cause.getCause() instanceof BadPacketException)
                    {
                        FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - bad packet, are mods in use!? {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else if ( cause.getCause() instanceof OverflowPacketException)
                    {
                        FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else
                    {
                        FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - could not decode packet! {1}", new Object[]
                        {
                            handler, cause.getCause() != null ? cause.getCause() : cause
                        } );
                    }
                } else if ( cause instanceof IOException || ( cause instanceof IllegalStateException && handler instanceof InitialHandler ) )
                {
                    FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.WARNING, "{0} - {1}: {2}", new Object[]
                    {
                        handler, cause.getClass().getSimpleName(), cause.getMessage()
                    } );
                } else if ( cause instanceof QuietException )
                {
                    FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.SEVERE, "{0} - encountered exception: {1}", new Object[]
                    {
                        handler, cause
                    } );
                } else
                {
                    FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.SEVERE, handler + " - encountered exception", cause );
                }
            }

            if ( handler != null )
            {
                try
                {
                    handler.exception( cause );
                } catch ( Exception ex )
                {
                    FoxServer.getInstance().getLogger().log( com.foxprox.logger.api.Level.SEVERE, handler + " - exception processing exception", ex );
                }
            }

            ctx.close();
        }
    }
}
