package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.Property;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginSuccess extends DefinedPacket
{

    private UUID uuid;
    private String username;
    private Property[] properties;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_2 ) {
            uuid = readUndashedUUID( buf );
        } else
        // FlameCord end - 1.7.x support
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            uuid = readUUID( buf );
        } else
        {
            uuid = UUID.fromString( readString( buf ) );
        }
        username = readString( buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            properties = readProperties( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_7_2 ) {
            writeUndashedUUID( uuid.toString(), buf );
        } else
        // FlameCord end - 1.7.x support
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16 )
        {
            writeUUID( uuid, buf );
        } else
        {
            writeString( uuid.toString(), buf );
        }
        writeString( username, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            writeProperties( properties, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    // FlameCord start - 1.7.x support
    private static UUID readUndashedUUID(ByteBuf buf) {
        return UUID.fromString( new StringBuilder( readString( buf ) ).insert( 20, '-' ).insert( 16, '-' ).insert( 12, '-' ).insert( 8, '-' ).toString() );
    }

    private static void writeUndashedUUID(String uuid, ByteBuf buf) {
        writeString( new StringBuilder( 32 ).append( uuid, 0, 8 ).append( uuid, 9, 13 ).append( uuid, 14, 18 ).append( uuid, 19, 23 ).append( uuid, 24, 36 ).toString(), buf );
    }
    // FlameCord end - 1.7.x support
}
