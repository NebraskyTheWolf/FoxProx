package com.foxprox.network.proxy.tab;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.networking.connection.LoginResult;
import com.foxprox.network.proxy.networking.protocol.Property;
import com.foxprox.network.proxy.networking.protocol.packet.PlayerListItem;
import lombok.RequiredArgsConstructor;
import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.UserConnection;

@RequiredArgsConstructor
public abstract class TabList
{

    protected final FoxPlayer player;

    public abstract void onUpdate(PlayerListItem playerListItem);

    public abstract void onPingChange(int ping);

    public abstract void onServerChange();

    public abstract void onConnect();

    public abstract void onDisconnect();

    public static PlayerListItem rewrite(PlayerListItem playerListItem)
    {
        for ( PlayerListItem.Item item : playerListItem.getItems() )
        {
            if ( item.getUuid() == null ) // Old style ping
            {
                continue;
            }
            UserConnection player = FoxProx.getInstance().getPlayerByOfflineUUID( item.getUuid() );
            if ( player != null )
            {
                item.setUuid( player.getUniqueId() );
                LoginResult loginResult = player.getPendingConnection().getLoginProfile();
                if ( loginResult != null && loginResult.getProperties() != null )
                {
                    Property[] props = new Property[ loginResult.getProperties().length ];
                    for ( int i = 0; i < props.length; i++ )
                    {
                        props[i] = new Property( loginResult.getProperties()[i].getName(), loginResult.getProperties()[i].getValue(), loginResult.getProperties()[i].getSignature() );
                    }
                    item.setProperties( props );
                } else
                {
                    item.setProperties( new Property[ 0 ] );
                }
                if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER || playerListItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE )
                {
                    player.setGamemode( item.getGamemode() );
                }
                if ( playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER || playerListItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY )
                {
                    player.setPing( item.getPing() );
                }
            }
        }
        return playerListItem;
    }
}
