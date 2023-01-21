package com.foxprox.network.proxy.networking.protocol.packet;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EncryptionResponse extends DefinedPacket
{

    private byte[] sharedSecret;
    private byte[] verifyToken;
    private EncryptionData encryptionData;

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_7_6 ) )
        {
            sharedSecret = v17readArray( buf );
            verifyToken = v17readArray( buf );
            return;
        }
        // FlameCord end - 1.7.x support
        
        sharedSecret = readArray( buf, 128 );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19 || buf.readBoolean() )
        {
            verifyToken = readArray( buf, 128 );
        } else
        {
            encryptionData = new EncryptionData( buf.readLong(), readArray( buf ) );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_7_6 ) )
        {
            v17writeArray( sharedSecret, buf, false );
            v17writeArray( verifyToken, buf, false );
            return;
        }
        // FlameCord end - 1.7.x support

        writeArray( sharedSecret, buf );
        if ( verifyToken != null )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
            {
                buf.writeBoolean( true );
            }
            writeArray( verifyToken, buf );
        } else
        {
            buf.writeLong( encryptionData.getSalt() );
            writeArray( encryptionData.getSignature(), buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    public static class EncryptionData
    {

        private final long salt;
        private final byte[] signature;
    }

    // Waterfall start: Additional DoS mitigations, courtesy of Velocity
    public int expectedMaxLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        // It turns out these come out to the same length, whether we're talking >=1.8 or not.
        // The length prefix always winds up being 2 bytes.
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) return -1;
        return 260;
    }

    public int expectedMinLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        return expectedMaxLength(buf, direction, protocolVersion);
    }
    // Waterfall end
}
