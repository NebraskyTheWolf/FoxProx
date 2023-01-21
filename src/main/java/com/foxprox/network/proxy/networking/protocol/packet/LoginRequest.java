package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.PlayerPublicKey;
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
public class LoginRequest extends DefinedPacket
{

    private String data;
    private PlayerPublicKey publicKey;
    private UUID uuid;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        data = readString( buf, 16 );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            publicKey = readPublicKey( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            if ( buf.readBoolean() )
            {
                uuid = readUUID( buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( data, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            writePublicKey( publicKey, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19_1 )
        {
            if ( uuid != null )
            {
                buf.writeBoolean( true );
                writeUUID( uuid, buf );
            } else
            {
                buf.writeBoolean( false );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    // Waterfall start: Additional DoS mitigations, courtesy of Velocity
    public int expectedMaxLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        // Accommodate the rare (but likely malicious) use of UTF-8 usernames, since it is technically
        // legal on the protocol level.
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) return -1;
        return 1 + (16 * 4);
    }
    // Waterfall end
}
