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
public class Team extends DefinedPacket
{

    private String name;
    /**
     * 0 - create, 1 remove, 2 info update, 3 player add, 4 player remove.
     */
    private byte mode;
    private String displayName;
    private String prefix;
    private String suffix;
    private String nameTagVisibility;
    private String collisionRule;
    private int color;
    private byte friendlyFire;
    private String[] players;

    /**
     * Packet to destroy a team.
     *
     * @param name team name
     */
    public Team(String name)
    {
        this.name = name;
        this.mode = 1;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        name = readString( buf );
        mode = buf.readByte();
        if ( mode == 0 || mode == 2 )
        {
            displayName = readString( buf );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
            {
                prefix = readString( buf );
                suffix = readString( buf );
            }
            friendlyFire = buf.readByte();
            // FlameCord start - 1.7.x support
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_8 )
            {
                nameTagVisibility = readString( buf );
            }
            // FlameCord end - 1.7.x support
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                collisionRule = readString( buf );
            }
            // FlameCord start - 1.7.x support
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_8 )
            {
                color = ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 ) ? readVarInt( buf ) : buf.readByte();
            }
            // FlameCord end - 1.7.x support
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                prefix = readString( buf );
                suffix = readString( buf );
            }
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            int len = protocolVersion >= ProtocolConstants.MINECRAFT_1_8 ? readVarInt( buf ) : buf.readShort(); // FlameCord - 1.7.x support
            players = new String[ len ];
            for ( int i = 0; i < len; i++ )
            {
                players[i] = readString( buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_7_6 ) )
        {
            writeString( name, buf );
            buf.writeByte( mode );
            if ( mode == 0 || mode == 2 )
            {
                writeString( displayName, buf );
                writeString( prefix, buf );
                writeString( suffix, buf );
                buf.writeByte( friendlyFire );
            }
            if ( mode == 0 || mode == 3 || mode == 4 )
            {
                buf.writeShort( players.length );
                for ( String player : players )
                {
                    writeString( player, buf );
                }
            }
            return;
        }
        // FlameCord end - 1.7.x support
        
        writeString( name, buf );
        buf.writeByte( mode );
        if ( mode == 0 || mode == 2 )
        {
            writeString( displayName, buf );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
            {
                writeString( prefix, buf );
                writeString( suffix, buf );
            }
            buf.writeByte( friendlyFire );
            writeString( nameTagVisibility, buf );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                writeString( collisionRule, buf );
            }

            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                writeVarInt( color, buf );
                writeString( prefix, buf );
                writeString( suffix, buf );
            } else
            {
                buf.writeByte( color );
            }
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            writeVarInt( players.length, buf );
            for ( String player : players )
            {
                writeString( player, buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
