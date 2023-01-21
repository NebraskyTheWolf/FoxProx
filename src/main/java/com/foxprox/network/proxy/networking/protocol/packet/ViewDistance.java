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
public class ViewDistance extends DefinedPacket
{

    private int distance;

    @Override
    public void read(ByteBuf buf)
    {
        distance = DefinedPacket.readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        DefinedPacket.writeVarInt( distance, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
