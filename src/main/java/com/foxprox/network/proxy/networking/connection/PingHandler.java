package com.foxprox.network.proxy.networking.connection;

import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.networking.protocol.packet.Handshake;
import com.foxprox.network.proxy.networking.protocol.packet.StatusRequest;
import com.foxprox.network.proxy.networking.protocol.packet.StatusResponse;
import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.FoxServerInfo;
import com.foxprox.network.proxy.core.api.Callback;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.ServerPing;
import com.foxprox.network.proxy.core.netty.ChannelWrapper;
import com.foxprox.network.proxy.core.netty.PacketHandler;
import com.foxprox.network.proxy.core.netty.PipelineUtils;
import com.foxprox.network.proxy.networking.protocol.MinecraftDecoder;
import com.foxprox.network.proxy.networking.protocol.MinecraftEncoder;
import com.foxprox.network.proxy.networking.protocol.PacketWrapper;
import com.foxprox.network.proxy.networking.protocol.Protocol;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.util.BufUtil;
import com.foxprox.network.util.QuietException;

import java.util.UUID;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private final int protocol;
    private ChannelWrapper channel;

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.channel = channel;
        MinecraftEncoder encoder = new MinecraftEncoder( Protocol.HANDSHAKE, false, protocol );

        channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.STATUS, false, FoxServer.getInstance().getProtocolVersion() ) );
        channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, encoder );

        channel.write( new Handshake( protocol, target.getAddress().getHostString(), target.getAddress().getPort(), 1, false, UUID.randomUUID(), "") );

        encoder.setProtocol( Protocol.STATUS );
        channel.write( new StatusRequest() );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        callback.done( null, t );
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( packet.packet == null )
        {
            throw new QuietException( "Unexpected packet received during ping process! " + BufUtil.dump( packet.buf, 16 ) );
        }
    }

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    public void handle(StatusResponse statusResponse) throws Exception
    {
        Gson gson = protocol == ProtocolConstants.MINECRAFT_1_7_2 ? FoxProx.getInstance().gsonLegacy : FoxProx.getInstance().gson; // FlameCord - 1.7.x support
        ServerPing serverPing = gson.fromJson( statusResponse.getResponse(), ServerPing.class );
        ( (FoxServerInfo) target ).cachePing( serverPing );
        callback.done( serverPing, null );
        channel.close();
    }

    @Override
    public String toString()
    {
        return "[Ping Handler] -> " + target.getName();
    }
}
