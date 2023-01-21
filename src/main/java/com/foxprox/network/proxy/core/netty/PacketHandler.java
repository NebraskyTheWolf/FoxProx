package com.foxprox.network.proxy.core.netty;

import com.foxprox.network.proxy.networking.protocol.AbstractPacketHandler;
import com.foxprox.network.proxy.networking.protocol.PacketWrapper;

public abstract class PacketHandler extends AbstractPacketHandler
{

    @Override
    public abstract String toString();

    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return true;
    }

    public void exception(Throwable t) throws Exception
    {
    }

    public void handle(PacketWrapper packet) throws Exception
    {
    }

    public void connected(ChannelWrapper channel) throws Exception
    {
    }

    public void disconnected(ChannelWrapper channel) throws Exception
    {
    }

    public void writabilityChanged(ChannelWrapper channel) throws Exception
    {
    }
}
