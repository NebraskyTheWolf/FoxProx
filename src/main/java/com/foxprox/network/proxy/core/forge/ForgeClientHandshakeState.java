package com.foxprox.network.proxy.core.forge;

import java.util.Map;

import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.foxprox.network.proxy.ServerConnector;
import com.foxprox.network.proxy.UserConnection;

/**
 * Handshake sequence manager for the Bungee - Forge Client (Upstream) link.
 * Modelled after the Forge implementation. See
 * https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/handshake/FMLHandshakeClientState.java
 */
enum ForgeClientHandshakeState implements IForgeClientPacketHandler<ForgeClientHandshakeState>
{

    /**
     * Initiated at the start of a client handshake. This is a special case
     * where we don't want to use a {@link PluginMessage}, we're just sending
     * stuff out here.
     *
     * Transitions into the HELLO state upon completion.
     *
     * Requires: {@link UserConnection}.
     */
    START
    {
        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            con.unsafe().sendPacket( message );
            con.getForgeClientHandler().setState( HELLO );
            return HELLO;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            return HELLO;
        }
    },
    /**
     * Waiting to receive a client HELLO and the mod list. Upon receiving the
     * mod list, return the mod list of the server.
     *
     * We will be stuck in this state if we don't have a forge client. This is
     * OK.
     *
     * Transitions to the WAITINGCACK state upon completion.
     *
     * Requires:
     * {@link PluginMessage}, {@link UserConnection}, {@link ServerConnector}
     */
    HELLO
    {
        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            // Server Hello.
            if ( message.getData()[0] == 0 )
            {
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {

            System.out.println(message.getData()[0] + " ForgeClientHandshakeState::debug");

            // Client Hello.
            if ( message.getData()[0] == 1 )
            {
                return this;
            }

            // Mod list.
            if ( message.getData()[0] == 2 )
            {
                if ( con.getForgeClientHandler().getClientModList() == null )
                {
                    // This is the first Forge connection - so get the mods now.
                    // Once we've done it, no point doing it again.
                    Map<String, String> clientModList = ForgeUtils.readModList( message );
                    con.getForgeClientHandler().setClientModList( clientModList );
                    // FlameCord start - 1.7.x support
                    // If the user is below 1.8, we need to check the version of FML - it's not always an OK version.
                    if ( ProtocolConstants.isBeforeOrEq( con.getPendingConnection().getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) )
                    {
                        // Get the version from the mod list.
                        int buildNumber = ForgeUtils.getFmlBuildNumber( clientModList );

                        // If we get 0, we're probably using a testing build, so let it though. Otherwise, check the build number.
                        if ( buildNumber < ForgeConstants.FML_MIN_BUILD_VERSION && buildNumber != 0 )
                        {
                            // Mark the user as an old Forge user. This will then cause any Forge ServerConnectors to cancel any
                            // connections to it.
                            con.getForgeClientHandler().setForgeOutdated( true );
                        }
                    }
                    // FlameCord end - 1.7.x support
                }

                return WAITINGSERVERDATA;
            }

            return this;
        }

    },
    WAITINGSERVERDATA
    {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            // Mod list.
            if ( message.getData()[0] == 2 )
            {
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // ACK
            return WAITINGSERVERCOMPLETE;
        }
    },
    WAITINGSERVERCOMPLETE
    {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            // Mod ID's.
            if ( message.getData()[0] == 3 )
            {
                con.unsafe().sendPacket( message );
                return this;
            }

            con.unsafe().sendPacket( message ); // pass everything else
            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send ACK.
            return PENDINGCOMPLETE;
        }
    },
    PENDINGCOMPLETE
    {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if ( message.getData()[0] == -1 )
            {
                ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send an ACK
            return COMPLETE;
        }
    },
    COMPLETE
    {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if (( message.getTag().equals( ForgeConstants.FML_HANDSHAKE_TAG ) && message.getData()[0] == -1 )
                    || message.getTag().equals( ForgeConstants.FORGE_REGISTER ))
            {
                ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            return DONE;
        }
    },
    /**
     * Handshake has been completed. Ignores any future handshake packets, but not any FORGE packets.
     */
    DONE
    {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            if ( message.getTag().equals( ForgeConstants.FORGE_REGISTER ))
            {
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            return this;
        }
    }
}
