package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Chat extends DefinedPacket
{

    private static final UUID EMPTY_UUID = new UUID( 0L, 0L );
    private String message;
    private byte position;
    private UUID sender;

    public Chat(String message)
    {
        this( message, (byte) 0 );
    }

    public Chat(String message, byte position)
    {
        this( message, position, EMPTY_UUID );
    }

    public Chat(String message, byte position, UUID sender)
    {
        this.message = message;
        this.position = position;
        this.sender = sender == null ? EMPTY_UUID : sender;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        message = readString( buf, ( direction == ProtocolConstants.Direction.TO_CLIENT ) ? 262144 : ( protocolVersion >= ProtocolConstants.MINECRAFT_1_11 ? 256 : 100 ) );
        if ( ProtocolConstants.isAfterOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_8 ) ) // FlameCord - 1.7.x support
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            position = buf.readByte();
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
            {
                sender = readUUID( buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // Waterfall start
        if (direction == ProtocolConstants.Direction.TO_CLIENT) {
            writeString(this.message, Short.MAX_VALUE * 8 + 8, buf);
        } else
        // Waterfall end
        writeString( message, buf );
        if ( ProtocolConstants.isAfterOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_8 ) ) // FlameCord - 1.7.x support
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            buf.writeByte( position );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
            {
                writeUUID( sender, buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
