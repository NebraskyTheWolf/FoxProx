package com.foxprox.network.proxy;

import com.foxprox.logger.api.Level;
import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.*;
import com.foxprox.network.proxy.core.api.chat.BaseComponent;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.event.PermissionCheckEvent;
import com.foxprox.network.proxy.core.api.event.ServerConnectEvent;
import com.foxprox.network.proxy.core.api.permissions.DefaultPermission;
import com.foxprox.network.proxy.core.api.permissions.EntityPermissions;
import com.foxprox.network.proxy.core.api.score.Scoreboard;
import com.foxprox.network.proxy.networking.chat.ComponentSerializer;
import com.foxprox.network.proxy.networking.connection.InitialHandler;
import com.foxprox.network.proxy.core.forge.ForgeClientHandler;
import com.foxprox.network.proxy.core.forge.ForgeConstants;
import com.foxprox.network.proxy.core.forge.ForgeServerHandler;
import com.foxprox.network.proxy.core.netty.ChannelWrapper;
import com.foxprox.network.proxy.core.netty.HandlerBoss;
import com.foxprox.network.proxy.core.netty.PipelineUtils;
import com.foxprox.network.proxy.tab.ServerUnique;
import com.foxprox.network.proxy.tab.TabList;
import com.foxprox.network.util.CaseInsensitiveSet;
import com.foxprox.network.util.ChatComponentTransformer;
import com.foxprox.network.util.QuietException;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.MinecraftDecoder;
import com.foxprox.network.proxy.networking.protocol.MinecraftEncoder;
import com.foxprox.network.proxy.networking.protocol.PacketWrapper;
import com.foxprox.network.proxy.networking.protocol.Protocol;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.Chat;
import com.foxprox.network.proxy.networking.protocol.packet.ClientSettings;
import com.foxprox.network.proxy.networking.protocol.packet.Kick;
import com.foxprox.network.proxy.networking.protocol.packet.PlayerListHeaderFooter;
import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.foxprox.network.proxy.networking.protocol.packet.SetCompression;
import com.foxprox.network.proxy.networking.protocol.packet.SystemChat;
import net.samagames.persistanceapi.beans.players.PlayerBean;
import net.samagames.persistanceapi.beans.players.SanctionBean;

@RequiredArgsConstructor
public final class UserConnection implements FoxPlayer
{

    /*========================================================================*/
    @NonNull
    private final FoxServer bungee;
    @NonNull
    @Getter
    private final ChannelWrapper ch;
    @Getter
    @NonNull
    private final String name;
    @Getter
    private final InitialHandler pendingConnection;
    /*========================================================================*/
    @Getter
    @Setter
    private ServerConnection server;
    @Getter
    @Setter
    private Object dimension;
    @Getter
    @Setter
    private boolean dimensionChange = true;
    @Getter
    private final Collection<ServerInfo> pendingConnects = new HashSet<>();
    /*========================================================================*/
    @Getter
    @Setter
    private int ping = 100;
    @Getter
    @Setter
    private ServerInfo reconnectServer;
    @Getter
    private TabList tabListHandler;
    @Getter
    @Setter
    private int gamemode;
    @Getter
    private int compressionThreshold = -1;
    // Used for trying multiple servers in order
    @Setter
    private Queue<String> serverJoinQueue;
    /*========================================================================*/
    private final Collection<String> groups = new CaseInsensitiveSet();
    private final Collection<String> permissions = new CaseInsensitiveSet();
    /*========================================================================*/
    @Getter
    @Setter
    private int clientEntityId;
    @Getter
    @Setter
    private int serverEntityId;
    @Getter
    private ClientSettings settings;
    @Getter
    private final Scoreboard serverSentScoreboard = new Scoreboard();
    @Getter
    private final Collection<UUID> sentBossBars = new HashSet<>();
    // Waterfall start
    @Getter
    private final Multimap<Integer, Integer> potions = HashMultimap.create();
    // Waterfall end
    /*========================================================================*/
    @Getter
    private String displayName;
    private Locale locale;
    /*========================================================================*/
    @Getter
    @Setter
    private ForgeClientHandler forgeClientHandler;
    @Getter
    @Setter
    private ForgeServerHandler forgeServerHandler;
    /*========================================================================*/
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    // Hardcoded
    // SamaGames permissions implementation

    private final Object[] syncPermission = new Object[] {};

    private EntityPermissions handledPermissions;

    public void init()
    {
        this.displayName = name;

        tabListHandler = new ServerUnique( this );

        Collection<String> g = bungee.getConfigurationAdapter().getGroups( name );
        g.addAll( bungee.getConfigurationAdapter().getGroups( getUniqueId().toString() ) );
        for ( String s : g )
        {
            addGroups( s );
        }

        forgeClientHandler = new ForgeClientHandler( this );

        // No-config FML handshake marker.
        // Set whether the connection has a 1.8 FML marker in the handshake.
        if (this.getPendingConnection().getExtraDataInHandshake().contains( ForgeConstants.FML_HANDSHAKE_TOKEN ))
        {
            forgeClientHandler.setFmlTokenInHandshake( true );
        }
    }

    public void sendPacket(PacketWrapper packet)
    {
        ch.write( packet );
    }

    public boolean isActive()
    {
        return !ch.isClosed();
    }

    @Override
    public void setDisplayName(String name)
    {
        Preconditions.checkNotNull( name, "displayName" );
        // Its "spigot" responsability to choose wether to support 1.7 or not.
        //Preconditions.checkArgument( name.length() <= 16, "Display name cannot be longer than 16 characters" ); // FlameCord - 1.7.x support
        displayName = name;
    }

    @Override
    public void connect(ServerInfo target)
    {

        connect( target, null, ServerConnectEvent.Reason.PLUGIN );
    }

    @Override
    public void connect(ServerInfo target, ServerConnectEvent.Reason reason)
    {
        connect( target, null, false, reason );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback)
    {
        connect( target, callback, false, ServerConnectEvent.Reason.PLUGIN );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback, ServerConnectEvent.Reason reason)
    {
        connect( target, callback, false, reason );
    }

    @Deprecated
    public void connectNow(ServerInfo target)
    {
        connectNow( target, ServerConnectEvent.Reason.UNKNOWN );
    }

    public void connectNow(ServerInfo target, ServerConnectEvent.Reason reason)
    {
        dimensionChange = true;
        connect( target, reason );
    }

    public ServerInfo updateAndGetNextServer(ServerInfo currentTarget)
    {
        if ( serverJoinQueue == null )
        {
            serverJoinQueue = new LinkedList<>( getPendingConnection().getListener().getServerPriority() );
        }

        ServerInfo next = null;
        while ( !serverJoinQueue.isEmpty() )
        {
            ServerInfo candidate = FoxServer.getInstance().getServerInfo( serverJoinQueue.remove() );
            if ( !Objects.equals( currentTarget, candidate ) )
            {
                next = candidate;
                break;
            }
        }

        return next;
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry)
    {
        connect( info, callback, retry, ServerConnectEvent.Reason.PLUGIN );
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry, ServerConnectEvent.Reason reason)
    {
        // Waterfall start
        connect(info, callback, retry, reason, bungee.getConfig().getServerConnectTimeout());
    }
    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry, int timeout) {
        connect(info, callback, retry, ServerConnectEvent.Reason.PLUGIN, timeout);
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry, ServerConnectEvent.Reason reason, final int timeout) {
        this.connect(info, callback, retry, reason, timeout, true);
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry, ServerConnectEvent.Reason reason, final int timeout, boolean sendFeedback) {
        Preconditions.checkNotNull(info, "info");

        ServerConnectRequest.Builder builder = ServerConnectRequest.builder().retry( retry ).reason( reason ).target( info ).sendFeedback(sendFeedback); // Waterfall - feedback param
        builder.connectTimeout(timeout); // Waterfall
        if ( callback != null )
        {
            // Convert the Callback<Boolean> to be compatible with Callback<Result> from ServerConnectRequest.
            builder.callback( new Callback<ServerConnectRequest.Result>()
            {
                @Override
                public void done(ServerConnectRequest.Result result, Throwable error)
                {
                    callback.done( ( result == ServerConnectRequest.Result.SUCCESS ) ? Boolean.TRUE : Boolean.FALSE, error );
                }
            } );
        }

        connect( builder.build() );
    }

    @Override
    public void connect(final ServerConnectRequest request)
    {
        Preconditions.checkNotNull( request, "request" );

        final Callback<ServerConnectRequest.Result> callback = request.getCallback();
        ServerConnectEvent event = new ServerConnectEvent( this, request.getTarget(), request.getReason(), request );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.EVENT_CANCEL, null );
            }

            if ( getServer() == null && !ch.isClosing() )
            {
                // FlameCord - Change IllegalStateException to QuietException and explain that is a plugin
                throw new QuietException("A plugin cancelled ServerConnectEvent with no server or disconnect.");
            }
            return;
        }

        final FoxServerInfo target = (FoxServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.ALREADY_CONNECTED, null );
            }

            if (request.isSendFeedback()) sendMessage( bungee.getTranslation( "already_connected" ) ); // Waterfall
            return;
        }
        if ( pendingConnects.contains( target ) )
        {
            if ( callback != null )
            {
                callback.done( ServerConnectRequest.Result.ALREADY_CONNECTING, null );
            }

            if (request.isSendFeedback()) sendMessage( bungee.getTranslation( "already_connecting" ) ); // Waterfall
            return;
        }

        pendingConnects.add( target );

        ChannelInitializer initializer = new ChannelInitializer()
        {
            @Override
            protected void initChannel(Channel ch) throws Exception
            {
                PipelineUtils.BASE.initChannel( ch );
                ch.pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( bungee, UserConnection.this, target ) );
            }
        };
        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            @SuppressWarnings("ThrowableResultIgnored")
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( callback != null )
                {
                    callback.done( ( future.isSuccess() ) ? ServerConnectRequest.Result.SUCCESS : ServerConnectRequest.Result.FAIL, future.cause() );
                }

                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );

                    ServerInfo def = updateAndGetNextServer( target );
                    if ( request.isRetry() && def != null && ( getServer() == null || def != getServer().getInfo() ) )
                    {
                        if (request.isSendFeedback()) sendMessage( bungee.getTranslation( "fallback_lobby" ) ); // Waterfall
                        connect( def, null, true, ServerConnectEvent.Reason.LOBBY_FALLBACK, request.getConnectTimeout(), request.isSendFeedback() ); // Waterfall
                    } else if ( dimensionChange )
                    {
                        disconnect( bungee.getTranslation( "fallback_kick", connectionFailMessage( future.cause() ) ) );
                    } else
                    {
                        if (request.isSendFeedback()) sendMessage( bungee.getTranslation( "fallback_kick", connectionFailMessage( future.cause() ) ) );
                    }
                }
            }
        };
        Bootstrap b = new Bootstrap()
                .channel( PipelineUtils.getChannel( target.getAddress() ) )
                .group( ch.getHandle().eventLoop() )
                .handler( initializer )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, request.getConnectTimeout() )
                .remoteAddress( target.getAddress() );
        // Windows is bugged, multi homed users will just have to live with random connecting IPs
        // FlameCord - Use listenerInfo
        final ListenerInfo listenerInfo = getPendingConnection().getListener();
        if ( listenerInfo.isSetLocalAddress() && !PlatformDependent.isWindows() && listenerInfo.getSocketAddress() instanceof InetSocketAddress )
        {
            b.localAddress( listenerInfo.getHost().getHostString(), 0 );
        }
        b.connect().addListener( listener );
    }

    private String connectionFailMessage(Throwable cause)
    {
        return FoxProx.PROXY_TAG + ChatColor.YELLOW + "A error occurred during Client ACK.";
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect0( TextComponent.fromLegacyText( reason ) );
    }

    @Override
    public void disconnect(BaseComponent... reason)
    {
        disconnect0( reason );
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect0( reason );
    }

    public void disconnect0(final BaseComponent... reason)
    {
        if ( !ch.isClosing() )
        {
            bungee.getLogger().log( Level.INFO, "[{0}] disconnected with: {1}", new Object[]
            {
                getName(), BaseComponent.toLegacyText( reason )
            } );

            ch.close( new Kick( ComponentSerializer.toString( reason ) ) );
            System.gc();

            if ( server != null )
            {
                server.setObsolete( true );
                server.disconnect( "Quitting" );
                System.gc();
            }
        }
    }

    @Override
    public void chat(String message)
    {
        Preconditions.checkState( server != null, "Not connected to server" );
        if ( getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19 )
        {
            throw new UnsupportedOperationException( "Cannot spoof chat on this client version!" );
        }
        server.getCh().write( new Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        sendMessage( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void sendMessages(String... messages)
    {
        for ( String message : messages )
        {
            sendMessage( message );
        }
    }

    @Override
    public void sendMessage(BaseComponent... message)
    {
        sendMessage( ChatMessageType.SYSTEM, message );
    }

    @Override
    public void sendMessage(BaseComponent message)
    {
        sendMessage( ChatMessageType.SYSTEM, message );
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent... message)
    {
        sendMessage( position, null, message );
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent message)
    {
        sendMessage( position, (UUID) null, message );
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent... message)
    {
        sendMessage( ChatMessageType.CHAT, sender, message );
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent message)
    {
        sendMessage( ChatMessageType.CHAT, sender, message );
    }

    private void sendMessage(ChatMessageType position, UUID sender, String message)
    {
        if ( getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19 )
        {
            // Align with Spigot and remove client side formatting for now
            if ( position == ChatMessageType.CHAT )
            {
                position = ChatMessageType.SYSTEM;
            }

            unsafe().sendPacket( new SystemChat( message, position.ordinal() ) );
        } else
        {
            unsafe().sendPacket( new Chat( message, (byte) position.ordinal(), sender ) );
        }
    }

    private void sendMessage(ChatMessageType position, UUID sender, BaseComponent... message)
    {
        // transform score components
        message = ChatComponentTransformer.getInstance().transform( this, true, message );

        if ( position == ChatMessageType.ACTION_BAR && getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_17 && getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_8 ) // FlameCord - 1.7.x support
        {
            // Versions older than 1.11 cannot send the Action bar with the new JSON formattings
            // Fix by converting to a legacy message, see https://bugs.mojang.com/browse/MC-119145
            if ( getPendingConnection().getVersion() <= ProtocolConstants.MINECRAFT_1_10 )
            {
                sendMessage( position, sender, ComponentSerializer.toString( new TextComponent( BaseComponent.toLegacyText( message ) ) ) );
            } else
            {
                com.foxprox.network.proxy.networking.protocol.packet.Title title = new com.foxprox.network.proxy.networking.protocol.packet.Title();
                title.setAction( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.ACTIONBAR );
                title.setText( ComponentSerializer.toString( message ) );
                unsafe.sendPacket( title );
            }
        } else
        {
            sendMessage( position, sender, ComponentSerializer.toString( message ) );
        }
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PluginMessage( channel, data, forgeClientHandler.isForgeUser() ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) getSocketAddress();
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return ch.getRemoteAddress();
    }

    @Override
    public Collection<String> getGroups()
    {
        return Collections.unmodifiableCollection( groups );
    }

    @Override
    public void addGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.add( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, true );
            }
        }
    }

    @Override
    public void removeGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.remove( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, false );
            }
        }
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return bungee.getPluginManager().callEvent( new PermissionCheckEvent( this, permission, permissions.contains( permission ) ) ).hasPermission();
    }

    @Override
    public void setPermission(String permission, boolean value)
    {
        if ( value )
        {
            permissions.add( permission );
        } else
        {
            permissions.remove( permission );
        }
    }

    @Override
    public Collection<String> getPermissions()
    {
        return Collections.unmodifiableCollection( permissions );
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public String getUUID()
    {
        return getPendingConnection().getUUID();
    }

    @Override
    public UUID getUniqueId()
    {
        return getPendingConnection().getUniqueId();
    }

    public void setSettings(ClientSettings settings)
    {
        this.settings = settings;
        this.locale = null;
    }

    @Override
    public Locale getLocale()
    {
        return ( locale == null && settings != null ) ? locale = Locale.forLanguageTag( settings.getLocale().replace( '_', '-' ) ) : locale;
    }

    @Override
    public byte getViewDistance()
    {
        return ( settings != null ) ? settings.getViewDistance() : 10;
    }

    @Override
    public FoxPlayer.ChatMode getChatMode()
    {
        if ( settings == null )
        {
            return FoxPlayer.ChatMode.SHOWN;
        }

        switch ( settings.getChatFlags() )
        {
            default:
            case 0:
                return FoxPlayer.ChatMode.SHOWN;
            case 1:
                return FoxPlayer.ChatMode.COMMANDS_ONLY;
            case 2:
                return FoxPlayer.ChatMode.HIDDEN;
        }
    }

    @Override
    public boolean hasChatColors()
    {
        return settings == null || settings.isChatColours();
    }

    @Override
    public SkinConfiguration getSkinParts()
    {
        return ( settings != null ) ? new PlayerSkinConfiguration( settings.getSkinParts() ) : PlayerSkinConfiguration.SKIN_SHOW_ALL;
    }

    @Override
    public FoxPlayer.MainHand getMainHand()
    {
        return ( settings == null || settings.getMainHand() == 1 ) ? FoxPlayer.MainHand.RIGHT : FoxPlayer.MainHand.LEFT;
    }

    @Override
    public boolean isForgeUser()
    {
        return forgeClientHandler.isForgeUser();
    }

    @Override
    public Map<String, String> getModList()
    {
        if ( forgeClientHandler.getClientModList() == null )
        {
            // Return an empty map, rather than a null, if the client hasn't got any mods,
            // or is yet to complete a handshake.
            return ImmutableMap.of();
        }

        return ImmutableMap.copyOf( forgeClientHandler.getClientModList() );
    }

    @Override
    public void setTabHeader(BaseComponent header, BaseComponent footer)
    {
        if ( ProtocolConstants.isBeforeOrEq( pendingConnection.getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) ) return; // FlameCord
        header = ChatComponentTransformer.getInstance().transform( this, true, header )[0];
        footer = ChatComponentTransformer.getInstance().transform( this, true, footer )[0];

        unsafe().sendPacket( new PlayerListHeaderFooter(
                ComponentSerializer.toString( header ),
                ComponentSerializer.toString( footer )
        ) );
    }

    @Override
    public void setTabHeader(BaseComponent[] header, BaseComponent[] footer)
    {
        if ( ProtocolConstants.isBeforeOrEq( pendingConnection.getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) ) return; // FlameCord
        header = ChatComponentTransformer.getInstance().transform( this, true, header );
        footer = ChatComponentTransformer.getInstance().transform( this, true, footer );

        unsafe().sendPacket( new PlayerListHeaderFooter(
                ComponentSerializer.toString( header ),
                ComponentSerializer.toString( footer )
        ) );
    }

    @Override
    public void resetTabHeader()
    {
        // Mojang did not add a way to remove the header / footer completely, we can only set it to empty
        setTabHeader( (BaseComponent) null, null );
    }

    @Override
    public void sendTitle(Title title)
    {
        title.send( this );
    }

    public String getExtraDataInHandshake()
    {
        return this.getPendingConnection().getExtraDataInHandshake();
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        if ( ProtocolConstants.isBeforeOrEq( pendingConnection.getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) ) return; // FlameCord
        if ( !ch.isClosing() && this.compressionThreshold == -1 && compressionThreshold >= 0 ) {
            this.compressionThreshold = compressionThreshold;
            unsafe.sendPacket( new SetCompression( compressionThreshold ) );
            ch.setCompressionThreshold( compressionThreshold );
        }
    }

    @Override
    public boolean isConnected()
    {
        return !ch.isClosed();
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return serverSentScoreboard;
    }

    @Override
    public PluginMessage ClientBrandName() {
        return pendingConnection.getBrandMessage();
    }

    @Override
    public EntityPermissions getPermission() {
        return this.handledPermissions;
    }

    @Override
    public void setPermissions(EntityPermissions permissions) {
        synchronized (syncPermission) {
            this.handledPermissions = permissions;
        }
    }

    @Override
    public void resetPermissions() {
        this.handledPermissions = new DefaultPermission(this.getUniqueId());
    }

    @Override
    public void updatePermissions(EntityPermissions permissions) {
        this.handledPermissions = permissions;
    }

    @Override
    public PlayerBean handledPlayer() throws Exception {
        return FoxServer
                .getInstance()
                .getGameServiceManager()
                .getPlayer(this.getUniqueId());
    }

    @Override
    public void createHandledPlayer() throws Exception {
        FoxServer
                .getInstance()
                .getGameServiceManager()
                .createPlayer(new PlayerBean(
                        this.getUniqueId(),
                        this.getName(),
                        null,
                        500,
                        0,
                        0,
                        new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis()),
                        this.getPendingConnection().getListener().getSocketAddress().toString(),
                        null,
                        1
                ));
    }

    @Override
    public void updateHandledPlayer() throws Exception {
        PlayerBean player = this.handledPlayer();
        FoxServer.getInstance()
                .getGameServiceManager()
                .updatePlayer(new PlayerBean(
                        this.getUniqueId(),
                        this.getName(),
                        null,
                        player.getCoins(),
                        player.getStars(),
                        player.getPowders(),
                        new Timestamp(System.currentTimeMillis()),
                        player.getFirstLogin(),
                        this.getPendingConnection().getListener().getSocketAddress().toString(),
                        player.getTopTpKey(),
                        player.getGroupId()
                ));
    }

    @Override
    public boolean isBanned() throws Exception {
        SanctionBean banned = FoxServer
                .getInstance()
                .getGameServiceManager()
                .getPlayerBanned(this.handledPlayer());
        return (banned != null && !banned.isDeleted());
    }

    @Override
    public boolean connectToLobby(int hubId) {
        if (FoxServer.getInstance().getLobbyManager().isServerAvailable()) {
            this.connect(FoxServer.getInstance().getLobbyManager().fetchAllLobby().get(hubId));
            return true;
        } else {
            return false;
        }
    }
}
