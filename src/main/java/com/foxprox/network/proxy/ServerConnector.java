package com.foxprox.network.proxy;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.event.ServerConnectEvent;
import com.foxprox.network.proxy.core.api.event.ServerConnectedEvent;
import com.foxprox.network.proxy.core.api.event.ServerKickEvent;
import com.foxprox.network.proxy.core.api.event.ServerSwitchEvent;
import com.foxprox.network.proxy.core.api.score.Scoreboard;
import com.foxprox.network.proxy.networking.chat.ComponentSerializer;
import com.foxprox.network.proxy.networking.connection.CancelSendSignal;
import com.foxprox.network.proxy.networking.connection.DownstreamBridge;
import com.foxprox.network.proxy.networking.connection.LoginResult;
import com.foxprox.network.proxy.core.forge.ForgeConstants;
import com.foxprox.network.proxy.core.forge.ForgeServerHandler;
import com.foxprox.network.proxy.core.forge.ForgeUtils;
import com.foxprox.network.proxy.core.netty.ChannelWrapper;
import com.foxprox.network.proxy.core.netty.HandlerBoss;
import com.foxprox.network.proxy.core.netty.PacketHandler;
import com.foxprox.network.proxy.core.netty.PipelineUtils;
import com.foxprox.network.proxy.networking.protocol.*;
import com.foxprox.network.proxy.networking.protocol.packet.*;
import com.foxprox.network.util.AddressUtil;
import com.foxprox.network.util.BufUtil;
import com.foxprox.network.util.QuietException;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Arrays; // Waterfall
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerConnector extends PacketHandler
{

    private final FoxServer bungee;
    private ChannelWrapper ch;
    private final UserConnection user;
    private final FoxServerInfo target;
    private State thisState = State.LOGIN_SUCCESS;
    @Getter
    private ForgeServerHandler handshakeHandler;
    private boolean obsolete;

    private enum State
    {

        LOGIN_SUCCESS, ENCRYPT_RESPONSE, LOGIN, FINISHED;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        if ( obsolete )
        {
            return;
        }

        String message = "Exception Connecting:" + Util.exception( t );
        if ( user.getServer() == null )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( ChatColor.RED + message );
        }
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;

        this.handshakeHandler = new ForgeServerHandler( user, ch, target );
        Handshake originalHandshake = user.getPendingConnection().getHandshake();
        Handshake copiedHandshake = new Handshake( originalHandshake.getProtocolVersion(), originalHandshake.getHost(), originalHandshake.getPort(), 2);

        if ( FoxProx.getInstance().config.isIpForward() && user.getSocketAddress() instanceof InetSocketAddress )
        {
            String newHost = copiedHandshake.getHost() + "\00" + AddressUtil.sanitizeAddress( user.getAddress() ) + "\00" + user.getUUID();

            LoginResult profile = user.getPendingConnection().getLoginProfile();

            // Handle properties.
            Property[] properties = new Property[0];

            if ( profile != null && profile.getProperties() != null && profile.getProperties().length > 0 )
            {
                properties = profile.getProperties();
            }

            if ( user.getForgeClientHandler().isFmlTokenInHandshake() )
            {
                // Get the current properties and copy them into a slightly bigger array.
                Property[] newp = Arrays.copyOf( properties, properties.length + 2 );

                // Add a new profile property that specifies that this user is a Forge user.
                newp[newp.length - 2] = new Property( ForgeConstants.FML_LOGIN_PROFILE, "true", null );

                // If we do not perform the replacement, then the IP Forwarding code in Spigot et. al. will try to split on this prematurely.
                // FlameCord - Change replaceAll() to replace()
                newp[newp.length - 1] = new Property( ForgeConstants.EXTRA_DATA, user.getExtraDataInHandshake().replace( "\0", "\1"), "" );

                // All done.
                properties = newp;
            }

            // If we touched any properties, then append them
            if (properties.length > 0) {
                newHost += "\00" + FoxProx.getInstance().gson.toJson(properties);
            }

            copiedHandshake.setHost( newHost );
        } else if ( !user.getExtraDataInHandshake().isEmpty() )
        {
            // Restore the extra data
            copiedHandshake.setHost( copiedHandshake.getHost() + user.getExtraDataInHandshake() );
        }

        channel.write( copiedHandshake );

        channel.setProtocol( Protocol.LOGIN );
        channel.write( new LoginRequest( user.getName(), null, user.getUniqueId() ) );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        user.getPendingConnects().remove( target );
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( packet.packet == null )
        {
            throw new QuietException( "Unexpected packet received during server connector process!\n" + BufUtil.dump(packet.buf, 16) );
        }
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN_SUCCESS, "Not expecting LOGIN_SUCCESS" );
        ch.setProtocol( Protocol.GAME );
        thisState = State.LOGIN;

        // Only reset the Forge client when:
        // 1) The user is switching servers (so has a current server)
        // 2) The handshake is complete
        // 3) The user is currently on a modded server (if we are on a vanilla server,
        //    we may be heading for another vanilla server, so we don't need to reset.)
        //
        // user.getServer() gets the user's CURRENT server, not the one we are trying
        // to connect to.
        //
        // We will reset the connection later if the current server is vanilla, and
        // we need to switch to a modded connection. However, we always need to reset the
        // connection when we have a modded server regardless of where we go - doing it
        // here makes sense.
        if ( user.getServer() != null && user.getForgeClientHandler().isHandshakeComplete()
                && user.getServer().isForgeServer() )
        {
            user.getForgeClientHandler().resetHandshake();
        }

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(SetCompression setCompression) throws Exception
    {
        ch.setCompressionThreshold( setCompression.getThreshold() );
    }

    @Override
    public void handle(Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting LOGIN" );

        ServerConnection server = new ServerConnection( ch, target );
        ServerConnectedEvent event = new ServerConnectedEvent( user, server );

        if (server.isForgeServer() && user.isForgeUser()) {
            ((MinecraftDecoder) server.getCh().getHandle().pipeline().get(PipelineUtils.PACKET_DECODER)).setSupportsForge(true);
            ((MinecraftDecoder) user.getCh().getHandle().pipeline().get(PipelineUtils.PACKET_DECODER)).setSupportsForge(true);
        }

        bungee.getPluginManager().callEvent( event );

        ch.write( FoxProx.getInstance().registerChannels( user.getPendingConnection().getVersion() ) );
        Queue<DefinedPacket> packetQueue = target.getPacketQueue();
        synchronized ( packetQueue )
        {
            while ( !packetQueue.isEmpty() )
            {
                ch.write( packetQueue.poll() );
            }
        }

        PluginMessage brandMessage = user.getPendingConnection().getBrandMessage();
        if ( brandMessage != null )
        {
            ch.write( brandMessage );
        }

        Set<String> registeredChannels = user.getPendingConnection().getRegisteredChannels();
        if ( !registeredChannels.isEmpty() )
        {
            ch.write( new PluginMessage( user.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:register" : "REGISTER", Joiner.on( "\0" ).join( registeredChannels ).getBytes( StandardCharsets.UTF_8 ), false ) );
        }

        if ( user.getForgeClientHandler().getClientModList() == null && !user.getForgeClientHandler().isHandshakeComplete() ) // Vanilla
        {
            user.getForgeClientHandler().setHandshakeComplete();
        }

        if ( user.getServer() == null || !( login.getDimension() instanceof Integer ) )
        {
            // Once again, first connection
            user.setClientEntityId( login.getEntityId() );
            user.setServerEntityId( login.getEntityId() );

            // Set tab list size, TODO: what shall we do about packet mutability
            Login modLogin = new Login( login.getEntityId(), login.isHardcore(), login.getGameMode(), login.getPreviousGameMode(), login.getWorldNames(), login.getDimensions(), login.getDimension(), login.getWorldName(), login.getSeed(), login.getDifficulty(),
                    (byte) user.getPendingConnection().getListener().getTabListSize(), login.getLevelType(), login.getViewDistance(), login.getSimulationDistance(), login.isReducedDebugInfo(), login.isNormalRespawn(), login.isDebug(), login.isFlat(), login.getDeathLocation() );

            user.unsafe().sendPacket( modLogin );

            if ( user.getServer() != null )
            {
                user.getServer().setObsolete( true );
                user.getTabListHandler().onServerChange();

                user.getServerSentScoreboard().clear();

                for ( UUID bossbar : user.getSentBossBars() )
                {
                    // Send remove bossbar packet
                    user.unsafe().sendPacket( new BossBar( bossbar, 1 ) );
                }
                user.getSentBossBars().clear();

                user.unsafe().sendPacket( new Respawn( login.getDimension(), login.getWorldName(), login.getSeed(), login.getDifficulty(), login.getGameMode(), login.getPreviousGameMode(), login.getLevelType(), login.isDebug(), login.isFlat(), false, login.getDeathLocation() ) );
                user.getServer().disconnect( "Quitting" );
            } else
            {
                // FlameCord start - 1.7.x support
                String brandString = bungee.getName() + " (" + bungee.getVersion() + ")";

                if ( ProtocolConstants.isBeforeOrEq( user.getPendingConnection().getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) )
                {
                    user.unsafe().sendPacket( new PluginMessage( "MC|Brand", brandString.getBytes( StandardCharsets.UTF_8 ), handshakeHandler.isServerForge() ) );
                } else
                {
                    ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();
                    DefinedPacket.writeString( bungee.getName() + " (" + bungee.getVersion() + ")", brand );
                    user.unsafe().sendPacket( new PluginMessage( user.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand", brand, handshakeHandler.isServerForge() ) );
                    brand.release();
                }
                 // FlameCord end - 1.7.x support
            }

            user.setDimension( login.getDimension() );
        } else
        {
            user.getServer().setObsolete( true );
            user.getTabListHandler().onServerChange();

            Scoreboard serverScoreboard = user.getServerSentScoreboard();
            serverScoreboard.clear();

            for ( UUID bossbar : user.getSentBossBars() )
            {
                // Send remove bossbar packet
                user.unsafe().sendPacket( new BossBar( bossbar, 1 ) );
            }
            user.getSentBossBars().clear();

            // Update debug info from login packet
            user.unsafe().sendPacket( new EntityStatus( user.getClientEntityId(), login.isReducedDebugInfo() ? EntityStatus.DEBUG_INFO_REDUCED : EntityStatus.DEBUG_INFO_NORMAL ) );
            // And immediate respawn
            if ( user.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_15 )
            {
                user.unsafe().sendPacket( new GameState( GameState.IMMEDIATE_RESPAWN, login.isNormalRespawn() ? 0 : 1 ) );
            }

            user.setDimensionChange( true );

            user.setServerEntityId( login.getEntityId() );

            // Waterfall start
            // Ensure that we maintain consistency
            user.setClientEntityId( login.getEntityId() );

            // Only send if we are not in the same dimension
            if ( login.getDimension() != user.getDimension() ) // Waterfall - defer
            {
                user.unsafe().sendPacket( new Respawn( (Integer) user.getDimension() >= 0 ? -1 : 0, login.getWorldName(), login.getSeed(), login.getDifficulty(), login.getGameMode(), login.getPreviousGameMode(), login.getLevelType(), login.isDebug(), login.isFlat(), false, login.getDeathLocation() ) );
            }

            Login modLogin = new Login( login.getEntityId(), login.isHardcore(), login.getGameMode(), login.getPreviousGameMode(), login.getWorldNames(), login.getDimensions(), login.getDimension(), login.getWorldName(), login.getSeed(), login.getDifficulty(),
                    (byte) user.getPendingConnection().getListener().getTabListSize(), login.getLevelType(), login.getViewDistance(), login.getSimulationDistance(), login.isReducedDebugInfo(), login.isNormalRespawn(), login.isDebug(), login.isFlat(), login.getDeathLocation() );
            user.unsafe().sendPacket(modLogin);

            // Only send if we're in the same dimension
            if ( login.getDimension() == user.getDimension() ) // Waterfall - defer
            {
                user.unsafe().sendPacket( new Respawn( (Integer) login.getDimension() >= 0 ? -1 : 0, login.getWorldName(), login.getSeed(), login.getDifficulty(), login.getGameMode(), login.getPreviousGameMode(), login.getLevelType(), login.isDebug(), login.isFlat(), false, login.getDeathLocation() ) );
            }
            // Waterfall end
            user.unsafe().sendPacket( new Respawn( login.getDimension(), login.getWorldName(), login.getSeed(), login.getDifficulty(), login.getGameMode(), login.getPreviousGameMode(), login.getLevelType(), login.isDebug(), login.isFlat(), false, login.getDeathLocation() ) );

            if ( user.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_14 )
            {
                user.unsafe().sendPacket( new ViewDistance( login.getViewDistance() ) );
            }
            user.setDimension( login.getDimension() );

            // Remove from old servers
            user.getServer().disconnect( "Quitting" );
        }

        // TODO: Fix this?
        if ( !user.isActive() )
        {
            server.disconnect( "Quitting" );
            // Silly server admins see stack trace and die
            bungee.getLogger().warning( "No client connected for pending server!" );
            return;
        }

        // Add to new server
        // TODO: Move this to the connected() method of DownstreamBridge
        target.addPlayer( user );
        user.getPendingConnects().remove( target );
        user.setServerJoinQueue( null );
        user.setDimensionChange( false );

        ServerInfo from = ( user.getServer() == null ) ? null : user.getServer().getInfo();
        user.setServer( server );
        ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new DownstreamBridge( bungee, user, server ) );

        bungee.getPluginManager().callEvent( new ServerSwitchEvent( user, from ) );

        thisState = State.FINISHED;

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(EncryptionRequest encryptionRequest) throws Exception
    {
        throw new QuietException( "Server is online mode!" );
    }

    @Override
    public void handle(Kick kick) throws Exception
    {
        ServerInfo def = user.updateAndGetNextServer( target );
        ServerKickEvent event = new ServerKickEvent( user, target, ComponentSerializer.parse( kick.getMessage() ), def, ServerKickEvent.State.CONNECTING, ServerKickEvent.Cause.SERVER );  // Waterfall
        if ( event.getKickReason().toLowerCase( Locale.ROOT ).contains( "outdated" ) && def != null )
        {
            // Pre cancel the event if we are going to try another server
            event.setCancelled( true );
        }
        bungee.getPluginManager().callEvent( event );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            obsolete = true;
            user.connect( event.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT );
            throw CancelSendSignal.INSTANCE;
        }

        String message = bungee.getTranslation( "connect_kick", target.getName(), event.getKickReason() );
        if ( user.isDimensionChange() )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( message );
        }

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        if ( FoxProx.getInstance().config.isForgeSupport() )
        {
            if ( pluginMessage.getTag().equals( ForgeConstants.FML_REGISTER ) )
            {
                Set<String> channels = ForgeUtils.readRegisteredChannels( pluginMessage );
                boolean isForgeServer = false;
                for ( String channel : channels )
                {
                    if ( channel.equals( ForgeConstants.FML_HANDSHAKE_TAG ) )
                    {
                        // If we have a completed handshake and we have been asked to register a FML|HS
                        // packet, let's send the reset packet now. Then, we can continue the message sending.
                        // The handshake will not be complete if we reset this earlier.
                        if ( user.getServer() != null && user.getForgeClientHandler().isHandshakeComplete() )
                        {
                            user.getForgeClientHandler().resetHandshake();
                        }

                        isForgeServer = true;
                        break;
                    }
                }

                if ( isForgeServer && !this.handshakeHandler.isServerForge() )
                {
                    // We now set the server-side handshake handler for the client to this.
                    handshakeHandler.setServerAsForgeServer();
                    user.setForgeServerHandler( handshakeHandler );
                }
            }

            if ( pluginMessage.getTag().equals( ForgeConstants.FML_HANDSHAKE_TAG ) || pluginMessage.getTag().equals( ForgeConstants.FORGE_REGISTER ) )
            {
                this.handshakeHandler.handle( pluginMessage );
                // FlameCord start - 1.7.x support
                if ( user.getForgeClientHandler().checkUserOutdated() )
                {
                    ch.close();
                    user.getPendingConnects().remove(target);
                }
                // FlameCord end - 1.7.x support
                // We send the message as part of the handler, so don't send it here.
                throw CancelSendSignal.INSTANCE;
            }
        }

        // We have to forward these to the user, especially with Forge as stuff might break
        // This includes any REGISTER messages we intercepted earlier.
        user.unsafe().sendPacket( pluginMessage );
    }

    @Override
    public void handle(LoginPayloadRequest loginPayloadRequest)
    {
        ch.write( new LoginPayloadResponse( loginPayloadRequest.getId(), null ) );
    }

    @Override
    public String toString()
    {
        return "[" + user.getName() + "|" + user.getAddress() + "] <-> ServerConnector [" + target.getName() + "]";
    }
}
