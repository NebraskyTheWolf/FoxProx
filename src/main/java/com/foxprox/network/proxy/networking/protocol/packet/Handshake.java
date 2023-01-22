package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends DefinedPacket
{

    private int protocolVersion;
    private String host;
    private int port;
    private int requestedProtocol;
    @Override
    public void read(ByteBuf buf)
    {
        this.protocolVersion = readVarInt( buf );
        this.host = readString( buf, 255 );
        this.port = buf.readUnsignedShort();
        this.requestedProtocol = readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( this.protocolVersion, buf );
        writeString( this.host, buf );
        buf.writeShort( this.port );
        writeVarInt( this.requestedProtocol, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Override
    public int expectedMaxLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        return 1036 + 512; // FlameCord - Apply packet limits
    }

    @Override
    public int expectedMinLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        return 5; // FlameCord - Apply packet limits
    }
}
