package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.OverflowPacketException;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginPayloadResponse extends DefinedPacket
{

    private int id;
    private byte[] data;

    @Override
    public void read(ByteBuf buf)
    {
        id = readVarInt( buf );

        if ( buf.readBoolean() )
        {
            int len = buf.readableBytes();
            if ( len > 1048576 )
            {
                throw new OverflowPacketException( "Payload may not be larger than 1048576 bytes" );
            }
            data = new byte[ len ];
            buf.readBytes( data );
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( id, buf );
        if ( data != null )
        {
            buf.writeBoolean( true );
            buf.writeBytes( data );
        } else
        {
            buf.writeBoolean( false );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
