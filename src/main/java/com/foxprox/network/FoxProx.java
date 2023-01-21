package com.foxprox.network;

import com.foxprox.logger.Logger;
import com.foxprox.logger.api.Level;
import com.foxprox.network.command.ConsoleCommandSender;
import com.foxprox.network.command.foxprox.admin.CommandEnd;
import com.foxprox.network.command.foxprox.admin.CommandServer;
import com.foxprox.network.command.foxprox.mod.chat.CommandChannel;
import com.foxprox.network.command.foxprox.mod.chat.CommandSlow;
import com.foxprox.network.command.foxprox.mod.ingame.CommandBTP;
import com.foxprox.network.command.foxprox.mod.ingame.CommandCheck;
import com.foxprox.network.command.foxprox.mod.ingame.CommandMod;
import com.foxprox.network.command.foxprox.mod.ingame.CommandSTP;
import com.foxprox.network.command.foxprox.mod.sanction.*;
import com.foxprox.network.command.foxprox.player.CommandHub;
import com.foxprox.network.command.foxprox.player.CommandReport;
import com.foxprox.network.proxy.*;
import com.foxprox.network.proxy.conf.Configuration;
import com.foxprox.network.proxy.conf.YamlConfig;
import com.foxprox.network.proxy.core.api.*;
import com.foxprox.network.proxy.core.api.chat.*;
import com.foxprox.network.proxy.core.api.config.ConfigurationAdapter;
import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.permissions.EntityPermissions;
import com.foxprox.network.proxy.core.api.plugin.Plugin;
import com.foxprox.network.proxy.core.api.plugin.PluginManager;
import com.foxprox.network.proxy.core.forge.ForgeConstants;
import com.foxprox.network.proxy.core.netty.PipelineUtils;
import com.foxprox.network.proxy.lib.conf.WaterfallConfiguration;
import com.foxprox.network.proxy.lib.event.ProxyExceptionEvent;
import com.foxprox.network.proxy.lib.exception.ProxyPluginEnableDisableException;
import com.foxprox.network.proxy.networking.chat.*;
import com.foxprox.network.proxy.networking.compress.CompressFactory;
import com.foxprox.network.proxy.networking.listeners.*;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.foxprox.network.proxy.networking.query.RemoteQuery;
import com.foxprox.network.proxy.samagames.api.channels.JsonModMessage;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.proxy.samagames.api.pubsub.RedisEmitter;
import com.foxprox.network.proxy.samagames.databases.DatabaseConnector;
import com.foxprox.network.proxy.samagames.databases.RedisServer;
import com.foxprox.network.proxy.samagames.databases.impl.PubSubAPI;
import com.foxprox.network.proxy.scheduler.FoxScheduler;
import com.foxprox.network.util.CaseInsensitiveMap;
import com.foxprox.network.util.ModerationUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import net.samagames.persistanceapi.GameServiceManager;
import net.samagames.persistanceapi.beans.utils.BungeeConfigBean;
import redis.clients.jedis.Jedis;

/**
 * Main BungeeCord proxy class.
 */
public class FoxProx extends FoxServer
{

    public static final String PROXY_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "FoxGuard" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    @Getter
    public final Configuration config = new WaterfallConfiguration();
    /**
     * Localization bundle.
     */
    private ResourceBundle baseBundle;
    private ResourceBundle customBundle;
    public EventLoopGroup bossEventLoopGroup, workerEventLoopGroup;
    /**
     * locations.yml save thread.
     */
    private final Timer saveThread = new Timer( "Reconnect Saver" );
    // private final Timer metricsThread = new Timer( "Metrics Thread" ); // Waterfall: Disable Metrics
    /**
     * Server socket listener.
     */
    private final Collection<Channel> listeners = new HashSet<>();
    /**
     * Fully qualified connections.
     */
    private final Map<String, UserConnection> connections = new CaseInsensitiveMap<>();
    // Used to help with packet rewriting
    private final Map<UUID, UserConnection> connectionsByOfflineUUID = new HashMap<>();
    private final Map<UUID, UserConnection> connectionsByUUID = new HashMap<>();
    private final ReadWriteLock connectionLock = new ReentrantReadWriteLock();
    /**
     * Lock to protect the shutdown process from being triggered simultaneously
     * from multiple sources.
     */
    private final ReentrantLock shutdownLock = new ReentrantLock();
    /**
     * Plugin manager.
     */
    @Getter
    public final PluginManager pluginManager;
    @Getter
    @Setter
    private ReconnectHandler reconnectHandler;
    @Getter
    @Setter
    private ConfigurationAdapter configurationAdapter = new YamlConfig();
    private final Collection<String> pluginChannels = new HashSet<>();
    @Getter
    private final File pluginsFolder = new File( "plugins" );
    @Getter
    private final FoxScheduler scheduler = new FoxScheduler();
    // Waterfall start - Remove ConsoleReader for JLine 3 update
    /*
    @Getter
    private final ConsoleReader consoleReader;
    */
    // Waterfall end
    @Getter
    private final Logger logger;

    private GameServiceManager manager;
    private DatabaseConnector databaseConnector;
    private PubSubAPI pubSub;
    private BungeeConfigBean bungeeConfig;

    private PermissionsManager permissionsManager;
    private ServersListeners serversListeners;
    private ModerationListener moderationListener;
    private PlayerJoinListener playerJoinListener;
    private ProxyPingListener proxyPingListener;
    private NetworkListeners networkListeners;
    private LoadBalancerListener loadBalancerListener;
    private FetchLobby fetchLobby;

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter( BaseComponent.class, new ComponentSerializer() )
            .registerTypeAdapter( TextComponent.class, new TextComponentSerializer() )
            .registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() )
            .registerTypeAdapter( KeybindComponent.class, new KeybindComponentSerializer() )
            .registerTypeAdapter( ScoreComponent.class, new ScoreComponentSerializer() )
            .registerTypeAdapter( SelectorComponent.class, new SelectorComponentSerializer() )
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer() )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    // FlameCord start - 1.7.x support
    public final Gson gsonLegacy = new GsonBuilder()
            .registerTypeAdapter( BaseComponent.class, new ComponentSerializer() )
            .registerTypeAdapter( TextComponent.class, new TextComponentSerializer() )
            .registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() )
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer( ProtocolConstants.MINECRAFT_1_7_2 ) )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    // FlameCord end - 1.7.x support
    @Getter
    private ConnectionThrottle connectionThrottle;

    {
        registerChannel( "BungeeCord" );
    }

    public static FoxProx getInstance()
    {
        return (FoxProx) FoxServer.getInstance();
    }

    private ScheduledExecutorService executor;

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public FoxProx() throws IOException
    {
        Preconditions.checkState( new File( "." ).getAbsolutePath().indexOf( '!' ) == -1, "Cannot use FoxProx in directory with ! in path." );

        try
        {
            baseBundle = ResourceBundle.getBundle( "messages" );
        } catch ( MissingResourceException ex )
        {
            baseBundle = ResourceBundle.getBundle( "messages", Locale.ENGLISH );
        }
        reloadMessages();

        System.setProperty( "library.jansi.version", "BungeeCord" );

        logger = new Logger("FoxProx");
        // Waterfall end

        pluginManager = new PluginManager( this );
        // FlameCord - We register commands in our new method

        if ( !Boolean.getBoolean( "net.md_5.bungee.native.disable" ) )
        {
            if ( EncryptionUtil.nativeFactory.load() )
            {
                logger.info( "Using mbed TLS based native cipher." );
            } else
            {
                logger.info( "Using standard Java JCE cipher." );
            }
            if ( CompressFactory.zlib.load() )
            {
                logger.info( "Using zlib based native compressor." );
            } else
            {
                logger.info( "Using standard Java compressor." );
            }
        }
    }

    /**
     * Start this proxy instance by loading the configuration, plugins and
     * starting the connect thread.
     *
     * @throws Exception any critical errors encountered
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void start() throws Exception
    {
        System.setProperty( "io.netty.selectorAutoRebuildThreshold", "0" ); // Seems to cause Bungee to stop accepting connections
        if ( System.getProperty( "io.netty.leakDetectionLevel" ) == null && System.getProperty( "io.netty.leakDetection.level" ) == null )
        {
            ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.DISABLED ); // Eats performance
        }

        bossEventLoopGroup = PipelineUtils.newEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty Boss IO Thread #%1$d" ).build() );
        workerEventLoopGroup = PipelineUtils.newEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty Worker IO Thread #%1$d" ).build() );

        pluginsFolder.mkdir();
        pluginManager.detectPlugins( pluginsFolder );

        pluginManager.loadPlugins();
        config.load();



        // FlameCord - Renew and register modules
        final Collection<String> whitelistedAddresses = new HashSet<>();

        for (final ServerInfo serverInfo : getServers().values()) {
            whitelistedAddresses.add(serverInfo.getSocketAddress().toString());
        }

        whitelistedAddresses.add("45.155.168.25"); // WEBSITE

       // FlameCord.reload(logger, whitelistedAddresses);

        if (config.isForgeSupport()) {
            registerChannel( ForgeConstants.FML_TAG );
            registerChannel( ForgeConstants.FML_HANDSHAKE_TAG );
            registerChannel( ForgeConstants.FORGE_REGISTER );
        }

        isRunning = true;

        pluginManager.enablePlugins();

        if ( config.getThrottle() > 0 )
        {
            connectionThrottle = new ConnectionThrottle( config.getThrottle(), config.getThrottleLimit() );
        }
        startListeners();
        loadModules();

        saveThread.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                if ( getReconnectHandler() != null )
                {
                    getReconnectHandler().save();
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis( 5 ) );

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                independentThreadStop("The server has been restarted.", false );
            }
        } );

        if (config.getSqlUrl().length() < 16) {
            logger.severe("Database not configured.");
            System.exit(1);
        }

        if (config.getRedisPass().isEmpty()) {
            logger.warning("Redis password is not set!.");
            logger.warning(" -> All redis related functionality has been disabled.");
        }

        this.executor = Executors.newScheduledThreadPool(32);
        this.fetchLobby = new FetchLobby();

        this.manager = new GameServiceManager(config.getSqlUrl(), config.getSqlUser(), config.getSqlPass(), 1, 10);
        this.bungeeConfig = this.getGameServiceManager().getBungeeConfig();
        this.databaseConnector = new DatabaseConnector(this, new RedisServer(
                config.getRedisHost(),
                config.getRedisPort(),
                config.getRedisPass()
        ));
        this.pubSub = new PubSubAPI(this);

        this.pubSub.subscribe("*", new RedisEmitter());
        this.pubSub.subscribe("*", new RedisEmitter.PatternEmitter());

        this.permissionsManager = new PermissionsManager();
        this.serversListeners = new ServersListeners();
        this.moderationListener = new ModerationListener();
        this.playerJoinListener = new PlayerJoinListener();
        this.proxyPingListener = new ProxyPingListener();
        this.networkListeners = new NetworkListeners();
        this.loadBalancerListener = new LoadBalancerListener();

        this.getPluginManager()
                .registerListener(this.permissionsManager);
        this.getPluginManager()
                .registerListener(this.serversListeners);
        this.getPluginManager()
                .registerListener(this.moderationListener);
        this.getPluginManager()
                .registerListener(this.playerJoinListener);
        this.getPluginManager()
                .registerListener(this.proxyPingListener);
        this.getPluginManager()
                .registerListener(this.networkListeners);
        this.getPluginManager()
                .registerListener(this.loadBalancerListener);
    }

    public void startListeners()
    {
        for ( final ListenerInfo info : config.getListeners() )
        {
            if ( info.isProxyProtocol() )
            {
                getLogger().log( Level.WARNING, "Using PROXY protocol for listener {0}, please ensure this listener is adequately firewalled.", info.getSocketAddress() );

                if ( connectionThrottle != null )
                {
                    connectionThrottle = null;
                    getLogger().log( Level.WARNING, "Since PROXY protocol is in use, internal connection throttle has been disabled." );
                }
            }

            ChannelFutureListener listener = new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if ( future.isSuccess() )
                    {
                        listeners.add( future.channel() );
                        getLogger().log( Level.INFO, "Listening on " + info.getSocketAddress() );
                    } else
                    {
                        getLogger().log( Level.WARNING, "Could not bind to host " + info.getSocketAddress(), future.cause() );
                    }
                }
            };
            new ServerBootstrap()
                    .channel( PipelineUtils.getServerChannel( info.getSocketAddress() ) )
                    .option( ChannelOption.SO_REUSEADDR, true ) // TODO: Move this elsewhere!
                    .childAttr( PipelineUtils.LISTENER, info )
                    .childHandler( PipelineUtils.SERVER_CHILD )
                    .group( bossEventLoopGroup, workerEventLoopGroup )
                    .localAddress( info.getSocketAddress() )
                    .bind().addListener( listener );

            if ( info.isQueryEnabled() )
            {
                Preconditions.checkArgument( info.getSocketAddress() instanceof InetSocketAddress, "Can only create query listener on UDP address" );

                ChannelFutureListener bindListener = new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if ( future.isSuccess() )
                        {
                            listeners.add( future.channel() );
                            getLogger().log( Level.INFO, "Started query on {0}", future.channel().localAddress() );
                        } else
                        {
                            getLogger().log( Level.WARNING, "Could not bind to host " + info.getSocketAddress(), future.cause() );
                        }
                    }
                };
                new RemoteQuery( this, info ).start( PipelineUtils.getDatagramChannel(), new InetSocketAddress( info.getHost().getAddress(), info.getQueryPort() ), workerEventLoopGroup, bindListener );
            }
        }
    }

    public void stopListeners()
    {
        for ( Channel listener : listeners )
        {
            getLogger().log( Level.INFO, "Closing listener {0}", listener );
            try
            {
                listener.close().syncUninterruptibly();
            } catch ( ChannelException ex )
            {
                getLogger().severe( "Could not close listen thread" );
            }
        }
        listeners.clear();

        this.databaseConnector.killConnection();
        this.pubSub.disable();
    }

    @Override
    public void stop()
    {
        stop( getTranslation( "restart" ) );
    }

    @Override
    public void stop(final String reason)
    {
        new Thread( "Shutdown Thread" )
        {
            @Override
            public void run()
            {
                independentThreadStop( reason, true );
            }
        }.start();
    }

    // This must be run on a separate thread to avoid deadlock!
    @SuppressFBWarnings("DM_EXIT")
    @SuppressWarnings("TooBroadCatch")
    private void independentThreadStop(final String reason, boolean callSystemExit)
    {
        // Acquire the shutdown lock
        // This needs to actually block here, otherwise running 'end' and then ctrl+c will cause the thread to terminate prematurely
        shutdownLock.lock();

        // Acquired the shutdown lock
        if ( !isRunning )
        {
            // Server is already shutting down - nothing to do
            shutdownLock.unlock();
            return;
        }
        isRunning = false;

        stopListeners();
        getLogger().info( "Closing pending connections" );

        connectionLock.readLock().lock();
        try
        {
            getLogger().log( Level.INFO, "Disconnecting {0} connections", connections.size() );
            for ( UserConnection user : connections.values() )
            {
                user.disconnect( reason );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }

        try
        {
            Thread.sleep( 500 );
        } catch ( InterruptedException ignored)
        {
        }

        if ( reconnectHandler != null )
        {
            getLogger().info( "Saving reconnect locations" );
            reconnectHandler.save();
            reconnectHandler.close();
        }
        saveThread.cancel();
        //metricsThread.cancel(); // Waterfall: Disable Metrics

        getLogger().info( "Disabling plugins" );
        for ( Plugin plugin : pluginManager.getPlugins() )
        {
            try
            {
                plugin.onDisable();
            } catch ( Throwable t )
            {
                // Waterfall start - throw exception event
                String msg = "Exception disabling plugin " + plugin.getDescription().getName();
                getLogger().log( Level.SEVERE, msg, t );
                pluginManager.callEvent( new ProxyExceptionEvent( new ProxyPluginEnableDisableException( msg, t, plugin) ) );
                // Waterfall end
            }
            getScheduler().cancel( plugin );
            plugin.getExecutorService().shutdownNow();
        }

        getLogger().info( "Closing IO threads" );
                bossEventLoopGroup.shutdownGracefully();
                workerEventLoopGroup.shutdownGracefully();
                while (true) {
                    try {
                        bossEventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        workerEventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        break;
                    } catch (InterruptedException ignored) {}
        }

        getLogger().info( "Thank you and goodbye" );
        // Need to close loggers after last message!
        org.apache.logging.log4j.LogManager.shutdown(); // Waterfall

        // Unlock the thread before optionally calling system exit, which might invoke this function again.
        // If that happens, the system will obtain the lock, and then see that isRunning == false and return without doing anything.
        shutdownLock.unlock();

        if ( callSystemExit )
        {
            System.exit( 0 );
        }
    }

    /**
     * Broadcasts a packet to all clients that is connected to this instance.
     *
     * @param packet the packet to send
     */
    public void broadcast(DefinedPacket packet)
    {
        connectionLock.readLock().lock();
        try
        {
            for ( UserConnection con : connections.values() )
            {
                con.unsafe().sendPacket( packet );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public String getName()
    {
        return "FoxProx";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

    public void reloadMessages()
    {
        File file = new File( "messages.properties" );
        if ( file.isFile() )
        {
            try ( FileReader rd = new FileReader( file ) )
            {
                customBundle = new PropertyResourceBundle( rd );
            } catch ( IOException ex )
            {
                getLogger().log( Level.SEVERE, "Could not load custom messages.properties", ex );
            }
        }
    }

    @Override
    public String getTranslation(String name, Object... args)
    {
        // FlameCord - Use own translation system
        return name; //getMessagesConfiguration().getTranslation(name, args);
    }

    @Override
    public Collection<FoxPlayer> getPlayers()
    {
        connectionLock.readLock().lock();
        try
        {
            return Collections.unmodifiableCollection( new HashSet<>( connections.values() ) );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public int getOnlineCount()
    {
        return connections.size();
    }

    @Override
    public FoxPlayer getPlayer(String name)
    {
        connectionLock.readLock().lock();
        try
        {
            return connections.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    public UserConnection getPlayerByOfflineUUID(UUID name)
    {
        // Waterfall start - Skip lock if we get a v4 UUID, we're not gonna be in here
        if (name != null && name.version() == 4) {
            return null;
        }
        // Waterfall end
        connectionLock.readLock().lock();
        try
        {
            return connectionsByOfflineUUID.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public FoxPlayer getPlayer(UUID uuid)
    {
        connectionLock.readLock().lock();
        try
        {
            return connectionsByUUID.get( uuid );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, ServerInfo> getServers()
    {
        return config.getServers();
    }

    // Waterfall start
    @Override
    public Map<String, ServerInfo> getServersCopy()
    {
        return config.getServersCopy();
    }
    // Waterfall end

    @Override
    public ServerInfo getServerInfo(String name)
    {
        return config.getServerInfo( name ); // Waterfall
    }

    @Override
    @Synchronized("pluginChannels")
    public void registerChannel(String channel)
    {
        pluginChannels.add( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public void unregisterChannel(String channel)
    {
        pluginChannels.remove( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public Collection<String> getChannels()
    {
        return Collections.unmodifiableCollection( pluginChannels );
    }

    public PluginMessage registerChannels(int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
        {
            return new PluginMessage( "minecraft:register", Util.format( Iterables.transform( pluginChannels, PluginMessage.MODERNISE ), "\00" ).getBytes( Charsets.UTF_8 ), false );
        }

        return new PluginMessage( "REGISTER", Util.format( pluginChannels, "\00" ).getBytes( Charsets.UTF_8 ), false );
    }

    @Override
    public int getProtocolVersion()
    {
        return ProtocolConstants.SUPPORTED_VERSION_IDS.get( ProtocolConstants.SUPPORTED_VERSION_IDS.size() - 1 );
    }

    @Override
    public String getGameVersion()
    {
        return getConfig().getGameVersion(); // Waterfall
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address, String motd, boolean restricted)
    {
        return constructServerInfo( name, (SocketAddress) address, motd, restricted );
    }

    @Override
    public ServerInfo constructServerInfo(String name, SocketAddress address, String motd, boolean restricted)
    {
        return new FoxServerInfo( name, address, motd, restricted );
    }

    @Override
    public CommandSender getConsole()
    {
        return ConsoleCommandSender.getInstance();
    }

    @Override
    public void broadcast(String message)
    {
        broadcast( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void broadcast(BaseComponent... message)
    {
        getConsole().sendMessage( BaseComponent.toLegacyText( message ) );
        for ( FoxPlayer player : getPlayers() )
        {
            player.sendMessage( message );
        }
    }

    @Override
    public void broadcast(BaseComponent message)
    {
        getConsole().sendMessage( message.toLegacyText() );
        for ( FoxPlayer player : getPlayers() )
        {
            player.sendMessage( message );
        }
    }

    public void addConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.put( con.getName(), con );
            connectionsByUUID.put( con.getUniqueId(), con );
            connectionsByOfflineUUID.put( con.getPendingConnection().getOfflineId(), con );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    public void removeConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            // TODO See #1218
            if ( connections.get( con.getName() ) == con )
            {
                connections.remove( con.getName() );
                connectionsByUUID.remove( con.getUniqueId() );
                connectionsByOfflineUUID.remove( con.getPendingConnection().getOfflineId() );
            }
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<String> getDisabledCommands()
    {
        return config.getDisabledCommands();
    }

    @Override
    public Collection<FoxPlayer> matchPlayer(final String partialName)
    {
        Preconditions.checkNotNull( partialName, "partialName" );

        FoxPlayer exactMatch = getPlayer( partialName );
        if ( exactMatch != null )
        {
            return Collections.singleton( exactMatch );
        }

        return Sets.newHashSet(getPlayers().stream().filter(input -> input != null && input.getName().toLowerCase(Locale.ROOT).startsWith(partialName.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
    }

    @Override
    public Title createTitle()
    {
        return new FoxTitle();
    }

    @Override
    public Jedis getRedisConnection() {
        Preconditions.checkNotNull(this.databaseConnector.getBungeeResource(), "FoxProx is not initialized.");
        return this.databaseConnector.getBungeeResource();
    }

    @Override
    public void publish(String channel, String message) {
        Preconditions.checkNotNull(this.pubSub, "pubSub is not initialized.");
        this.pubSub.send(channel, message);
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        Preconditions.checkNotNull(this.executor, "Executor can't be null");
        return this.executor;
    }

    @Override
    public EntityPermissions getPlayerPermissions(UUID player) {
        Preconditions.checkNotNull(player, "player can't be null");
        return this.permissionsManager.getPlayerPermission(player);
    }

    @Override
    public EntityPermissions getPlayerPermissions(FoxPlayer player) {
        Preconditions.checkNotNull(player, "player can't be null");
        return this.permissionsManager.getPlayerPermission(player.getUniqueId());
    }

    @Override
    public void sendModerationMessage(ModChannel channel, JsonModMessage message) {
        Preconditions.checkNotNull(channel, "channel can't be null");
        Preconditions.checkNotNull(message, "message can't be null");

        ModerationUtils.sendMessage(channel, message.getMessage(), UUID.randomUUID());
    }

    @Override
    public GameServiceManager getGameServiceManager() {
        Preconditions.checkNotNull(this.manager, "manager can't be null");
        return this.manager;
    }

    @Override
    public PubSubAPI getPubSub() {
        Preconditions.checkNotNull(this.pubSub, "pubSub can't be null");
        return this.pubSub;
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        Preconditions.checkNotNull(this.databaseConnector, "databaseConnector can't be null");
        return this.databaseConnector;
    }

    @Override
    public BungeeConfigBean getBungeeConfig() {
        Preconditions.checkNotNull(this.bungeeConfig, "bungeeConfig can't be null");
        return this.bungeeConfig;
    }

    @Override
    public FetchLobby getLobbyManager() {
        Preconditions.checkNotNull(this.fetchLobby, "fetchLobby can't be null");
        return this.fetchLobby;
    }

    // FlameCord - Method to simplify module registering
    public void loadModules() {
        pluginManager.registerCommand(null, new CommandReport());
        pluginManager.registerCommand(null, new CommandHub());

        pluginManager.registerCommand(null, new CommandEnd());
        pluginManager.registerCommand(null, new CommandServer());

        pluginManager.registerCommand(null, new CommandBan());
        pluginManager.registerCommand(null, new CommandHistory());
        pluginManager.registerCommand(null, new CommandKick());
        pluginManager.registerCommand(null, new CommandMute());
        pluginManager.registerCommand(null, new CommandWarn());

        pluginManager.registerCommand(null, new CommandSTP());
        pluginManager.registerCommand(null, new CommandMod());
        pluginManager.registerCommand(null, new CommandCheck());
        pluginManager.registerCommand(null, new CommandBTP());

        pluginManager.registerCommand(null, new CommandSlow());
        pluginManager.registerCommand(null, new CommandChannel());
    }
}
