package com.foxprox.network.proxy.core.api;

import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.PendingConnection;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.google.common.base.Preconditions;

public abstract class AbstractReconnectHandler implements ReconnectHandler
{

    @Override
    public ServerInfo getServer(FoxPlayer player)
    {
        ServerInfo server = getForcedHost( player.getPendingConnection() );
        if ( server == null )
        {
            server = getStoredServer( player );
            if ( server == null )
            {
                server = FoxServer.getInstance().getServerInfo( player.getPendingConnection().getListener().getDefaultServer() );
            }

            Preconditions.checkState( server != null, "Default server not defined" );
        }

        return server;
    }

    public static ServerInfo getForcedHost(PendingConnection con)
    {
        String forced = ( con.getVirtualHost() == null ) ? null : con.getListener().getForcedHosts().get( con.getVirtualHost().getHostString() );

        if ( forced == null && con.getListener().isForceDefault() )
        {
            forced = con.getListener().getDefaultServer();
        }
        return ( forced == null ) ? null : FoxServer.getInstance().getServerInfo( forced );
    }

    protected abstract ServerInfo getStoredServer(FoxPlayer player);
}
