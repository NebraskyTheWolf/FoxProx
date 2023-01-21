package com.foxprox.network.proxy.core.netty;

import com.foxprox.network.proxy.networking.compress.PacketCompressor;
import com.foxprox.network.proxy.networking.compress.PacketDecompressor;
import com.foxprox.network.proxy.networking.protocol.MinecraftDecoder;
import com.foxprox.network.proxy.networking.protocol.MinecraftEncoder;
import com.foxprox.network.proxy.networking.protocol.PacketWrapper;
import com.foxprox.network.proxy.networking.protocol.Protocol;
import com.foxprox.network.proxy.networking.protocol.packet.Kick;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.net.SocketAddress;

import lombok.Getter;
import lombok.Setter;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    @Setter
    private SocketAddress remoteAddress;
    @Getter
    private volatile boolean closed;
    @Getter
    private volatile boolean closing;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = ( this.ch.remoteAddress() == null ) ? this.ch.parent().localAddress() : this.ch.remoteAddress();
    }

    public void setProtocol(Protocol protocol)
    {
        // FlameCord - Use pipeline to reduce redundancy
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.get( MinecraftDecoder.class ).setProtocol( protocol );
        pipeline.get( MinecraftEncoder.class ).setProtocol( protocol );
    }

    public void setVersion(int protocol)
    {
        // FlameCord - Use pipeline to reduce redundancy
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.get( MinecraftDecoder.class ).setProtocolVersion( protocol );
        pipeline.get( MinecraftEncoder.class ).setProtocolVersion( protocol );
    }

    public void write(Object packet)
    {
        if ( !closed )
        {
            if ( packet instanceof PacketWrapper)
            {
                ( (PacketWrapper) packet ).setReleased( true );
                ch.writeAndFlush( ( (PacketWrapper) packet ).buf, ch.voidPromise() );
            } else
            {
                ch.writeAndFlush( packet, ch.voidPromise() );
            }
        }
    }

    public void markClosed()
    {
        closed = closing = true;
    }

    public void close()
    {
        close( null );
    }

    public void close(Object packet)
    {
        if ( !closed )
        {
            closed = closing = true;

            if ( packet != null && ch.isActive() )
            {
                // FlameCord - Remove the firing of exceptions on failure
                ch.writeAndFlush( packet ).addListeners( ChannelFutureListener.CLOSE );
            } else
            {
                // FlameCord - Don't flush just close
                ch.close();
            }
        }
    }

    // FlameCord - Deprecate and "disable" delayedClose because it doesn't have a reason to exist
    @Deprecated
    public void delayedClose(final Kick kick)
    {
        close(kick);
    }

    public void addBefore(String baseName, String name, ChannelHandler handler)
    {
        Preconditions.checkState( ch.eventLoop().inEventLoop(), "cannot add handler outside of event loop" );
        // FlameCord - Don't flush if not necessary
        ch.pipeline().addBefore( baseName, name, handler );
    }

    public Channel getHandle()
    {
        return ch;
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        // FlameCord - Use pipeline to reduce redundancy
        final ChannelPipeline pipeline = ch.pipeline();
        if ( pipeline.get( PacketCompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtils.PACKET_ENCODER, "compress", new PacketCompressor() );
        }
        if ( compressionThreshold != -1 )
        {
            pipeline.get( PacketCompressor.class ).setThreshold( compressionThreshold );
        } else
        {
            pipeline.remove( "compress" );
        }

        if ( pipeline.get( PacketDecompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtils.PACKET_DECODER, "decompress", new PacketDecompressor(compressionThreshold) );
        }
        if ( compressionThreshold == -1 )
        {
            pipeline.remove( "decompress" );
        }
    }
}
