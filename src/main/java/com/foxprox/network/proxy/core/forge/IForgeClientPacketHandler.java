package com.foxprox.network.proxy.core.forge;

import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.foxprox.network.proxy.UserConnection;

/**
 * An interface that defines a Forge Handshake Client packet.
 *
 * @param <S> The State to transition to.
 */
public interface IForgeClientPacketHandler<S>
{

    /**
     * Handles any {@link PluginMessage} packets.
     *
     * @param message The {@link PluginMessage} to handle.
     * @param con The {@link UserConnection} to send packets to.
     * @return The state to transition to.
     */
    public S handle(PluginMessage message, UserConnection con);

    /**
     * Sends any {@link PluginMessage} packets.
     *
     * @param message The {@link PluginMessage} to send.
     * @param con The {@link UserConnection} to set data.
     * @return The state to transition to.
     */
    public S send(PluginMessage message, UserConnection con);
}
