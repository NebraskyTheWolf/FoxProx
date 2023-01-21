package com.foxprox.network.proxy.conf;

import com.foxprox.network.proxy.core.api.config.ConfigurationAdapter;
import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.util.CaseInsensitiveMap;
import com.foxprox.network.util.CaseInsensitiveSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap; // Waterfall
import gnu.trove.map.TMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Synchronized; // Waterfall

import com.foxprox.network.proxy.core.api.Favicon;
import com.foxprox.network.proxy.core.api.ProxyConfig;
import com.foxprox.network.proxy.core.api.FoxServer;

/**
 * Core configuration for the proxy.
 */
@Getter
public abstract class Configuration implements ProxyConfig
{

    /**
     * Time before users are disconnected due to no network activity.
     */
    // FlameCord - Modify default timeout
    private int timeout = 17000;
    /**
     * UUID used for metrics.
     */
    private String uuid = UUID.randomUUID().toString();
    /**
     * Set of all listeners.
     */
    private Collection<ListenerInfo> listeners;
    private final Object serversLock = new Object(); // Waterfall
    /**
     * Set of all servers.
     */
    private TMap<String, ServerInfo> servers;
    /**
     * Should we check minecraft.net auth.
     */
    private boolean onlineMode = true;
    /**
     * Whether to check the authentication server public key.
     */
    private boolean enforceSecureProfile;
    /**
     * Whether we log proxy commands to the proxy log
     */
    private boolean logCommands;
    private boolean logPings = true;
    private int remotePingCache = -1;
    private int playerLimit = -1;
    private Collection<String> disabledCommands;
    private int serverConnectTimeout = 5000;
    private int remotePingTimeout = 5000;
    private int throttle = 4000;
    private int throttleLimit = 3;
    private boolean ipForward;
    private Favicon favicon;
    private int compressionThreshold = 256;
    private boolean preventProxyConnections;
    private boolean forgeSupport = true; // Waterfall: default to enabled

    private String sqlUrl  = "";
    private String sqlUser = "";
    private String sqlPass = "";

    private String redisHost = "";
    private Integer redisPort = 6379;
    private String redisPass = "";

    private Boolean isProduction = false;

    @Synchronized("serversLock") // Waterfall
    public void load()
    {
        ConfigurationAdapter adapter = FoxServer.getInstance().getConfigurationAdapter();
        adapter.load();

        File fav = new File( "server-icon.png" );
        if ( fav.exists() )
        {
            try
            {
                favicon = Favicon.create( ImageIO.read( fav ) );
            } catch ( IOException | IllegalArgumentException ex )
            {
                FoxServer.getInstance().getLogger().warning("Could not load server icon");
            }
        }

        listeners = adapter.getListeners();
        timeout = adapter.getInt( "timeout", timeout );
        uuid = adapter.getString( "stats", uuid );
        onlineMode = adapter.getBoolean( "online_mode", onlineMode );
        enforceSecureProfile = adapter.getBoolean( "enforce_secure_profile", enforceSecureProfile );
        logCommands = adapter.getBoolean( "log_commands", logCommands );
        logPings = adapter.getBoolean( "log_pings", logPings );
        remotePingCache = adapter.getInt( "remote_ping_cache", remotePingCache );
        playerLimit = adapter.getInt( "player_limit", playerLimit );
        serverConnectTimeout = adapter.getInt( "server_connect_timeout", serverConnectTimeout );
        remotePingTimeout = adapter.getInt( "remote_ping_timeout", remotePingTimeout );
        throttle = adapter.getInt( "connection_throttle", throttle );
        throttleLimit = adapter.getInt( "connection_throttle_limit", throttleLimit );
        ipForward = adapter.getBoolean( "ip_forward", ipForward );
        compressionThreshold = adapter.getInt( "network_compression_threshold", compressionThreshold );
        preventProxyConnections = adapter.getBoolean( "prevent_proxy_connections", preventProxyConnections );
        forgeSupport = adapter.getBoolean( "forge_support", forgeSupport );

        disabledCommands = new CaseInsensitiveSet( (Collection<String>) adapter.getList( "disabled_commands", Arrays.asList( "disabledcommandhere" ) ) );

        Preconditions.checkArgument( listeners != null && !listeners.isEmpty(), "No listeners defined." );

        servers = new CaseInsensitiveMap<>( adapter.getServers() );
    }

    @Override
    @Deprecated
    public String getFavicon()
    {
        return getFaviconObject().getEncoded();
    }

    @Override
    public Favicon getFaviconObject()
    {
        return favicon;
    }

    // Waterfall start
    @Override
    @Synchronized("serversLock")
    public Map<String, ServerInfo> getServersCopy() {
        return ImmutableMap.copyOf( servers );
    }

    @Override
    @Synchronized("serversLock")
    public ServerInfo getServerInfo(String name)
    {
        return this.servers.get( name );
    }

    @Override
    @Synchronized("serversLock")
    public ServerInfo addServer(ServerInfo server)
    {
        return this.servers.put( server.getName(), server );
    }

    @Override
    @Synchronized("serversLock")
    public boolean addServers(Collection<ServerInfo> servers)
    {
        boolean changed = false;
        for ( ServerInfo server : servers )
        {
            if ( server != this.servers.put( server.getName(), server ) ) changed = true;
        }
        return changed;
    }

    @Override
    @Synchronized("serversLock")
    public ServerInfo removeServerNamed(String name)
    {
        return this.servers.remove( name );
    }

    @Override
    @Synchronized("serversLock")
    public ServerInfo removeServer(ServerInfo server)
    {
        return this.servers.remove( server.getName() );
    }

    @Override
    @Synchronized("serversLock")
    public boolean removeServersNamed(Collection<String> names)
    {
        return this.servers.keySet().removeAll( names );
    }

    @Override
    @Synchronized("serversLock")
    public boolean removeServers(Collection<ServerInfo> servers)
    {
        boolean changed = false;
        for ( ServerInfo server : servers )
        {
            if ( null != this.servers.remove( server.getName() ) ) changed = true;
        }
        return changed;
    }

    public Boolean getProduction() {
        return isProduction;
    }

    // Waterfall end
}
