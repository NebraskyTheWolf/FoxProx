package com.foxprox.network.proxy.tab;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.PlayerListItem;

public class ServerUnique extends TabList
{

    private final Collection<UUID> uuids = new HashSet<>();
    private final Collection<String> usernames = new HashSet<>(); // FlameCord - 1.7.x support

    public ServerUnique(FoxPlayer player)
    {
        super( player );
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER )
            {
                // FlameCord start - 1.7.x support
                if ( item.getUuid() != null )
                {
                    uuids.add( item.getUuid() );
                } else
                {
                    usernames.add( item.getUsername() );
                }
                // FlameCord end - 1.7.x support
            } else if ( playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER )
            {
                // FlameCord start - 1.7.x support
                if ( item.getUuid() != null )
                {
                    uuids.remove( item.getUuid() );
                } else
                {
                    usernames.remove( item.getUsername() );
                }
                // FlameCord end - 1.7.x support
            }
        }
        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onPingChange(int ping)
    {

    }

    @Override
    public void onServerChange()
    {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction( PlayerListItem.Action.REMOVE_PLAYER );
        PlayerListItem.Item[] items = new PlayerListItem.Item[ uuids.size() + usernames.size() ]; // FlameCord - 1.7.x support
        int i = 0;

        // FlameCord start - 1.7.x support
        for ( String username : usernames )
        {
            PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
            item.setUsername( username );
            item.setDisplayName( username );
        }
        // FlameCord end - 1.7.x support

        for ( UUID uuid : uuids )
        {
            PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
            item.setUuid( uuid );
        }
        packet.setItems( items );
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isAfterOrEq( player.getPendingConnection().getVersion(), ProtocolConstants.MINECRAFT_1_8 ) )
        {
            player.unsafe().sendPacket( packet );
        } else
        {
            // Split up the packet
            for ( PlayerListItem.Item item : packet.getItems() )
            {
                PlayerListItem p2 = new PlayerListItem();
                p2.setAction( packet.getAction() );

                p2.setItems( new PlayerListItem.Item[]
                        {
                                item
                        } );
                player.unsafe().sendPacket( p2 );
            }
        }
        // FlameCord end - 1.7.x support
        uuids.clear();
        usernames.clear(); // FlameCord - 1.7.x support
    }

    @Override
    public void onConnect()
    {

    }

    @Override
    public void onDisconnect()
    {

    }
}
