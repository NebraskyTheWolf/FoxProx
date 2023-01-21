package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GameState extends DefinedPacket
{

    public static final short IMMEDIATE_RESPAWN = 11;
    //
    private short state;
    private float value;

    @Override
    public void read(ByteBuf buf)
    {
        state = buf.readUnsignedByte();
        value = buf.readFloat();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( state );
        buf.writeFloat( value );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
