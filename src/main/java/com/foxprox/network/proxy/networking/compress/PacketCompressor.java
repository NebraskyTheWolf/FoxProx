package com.foxprox.network.proxy.networking.compress;

import com.foxprox.network.proxy.jni.zlib.BungeeZlib;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import lombok.Setter;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf>
{

    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();
    @Setter
    private int threshold = 1024;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib.init( true, Deflater.DEFAULT_COMPRESSION );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception
    {
        int origSize = msg.readableBytes();
        if ( origSize < threshold )
        {
            DefinedPacket.writeVarInt( 0, out );
            out.writeBytes( msg );
        } else
        {
            DefinedPacket.writeVarInt( origSize, out );

            zlib.process( msg, out );
        }
    }
}
