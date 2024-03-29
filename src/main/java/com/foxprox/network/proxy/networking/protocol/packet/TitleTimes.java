package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TitleTimes extends DefinedPacket
{

    private int fadeIn;
    private int stay;
    private int fadeOut;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        fadeIn = buf.readInt();
        stay = buf.readInt();
        fadeOut = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeInt( fadeIn );
        buf.writeInt( stay );
        buf.writeInt( fadeOut );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
