package com.foxprox.network.proxy;

import com.foxprox.network.proxy.core.api.Title;
import com.foxprox.network.proxy.core.api.chat.BaseComponent;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.networking.chat.ComponentSerializer;
import com.foxprox.network.proxy.networking.protocol.DefinedPacket;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.foxprox.network.proxy.networking.protocol.packet.ClearTitles;
import com.foxprox.network.proxy.networking.protocol.packet.Subtitle;
import com.foxprox.network.proxy.networking.protocol.packet.TitleTimes;
import lombok.Data;

public class FoxTitle implements Title
{

    private TitlePacketHolder<com.foxprox.network.proxy.networking.protocol.packet.Title> title;
    private TitlePacketHolder<Subtitle> subtitle;
    private TitlePacketHolder<TitleTimes> times;
    private TitlePacketHolder<ClearTitles> clear;
    private TitlePacketHolder<ClearTitles> reset;

    @Data
    private static class TitlePacketHolder<T extends DefinedPacket>
    {

        private final com.foxprox.network.proxy.networking.protocol.packet.Title oldPacket;
        private final T newPacket;
    }

    private static TitlePacketHolder<TitleTimes> createAnimationPacket()
    {
        TitlePacketHolder<TitleTimes> title = new TitlePacketHolder<>( new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.TIMES ), new TitleTimes() );

        title.oldPacket.setFadeIn( 20 );
        title.oldPacket.setStay( 60 );
        title.oldPacket.setFadeOut( 20 );

        title.newPacket.setFadeIn( 20 );
        title.newPacket.setStay( 60 );
        title.newPacket.setFadeOut( 20 );

        return title;
    }

    @Override
    public Title title(BaseComponent text)
    {
        if ( title == null )
        {
            com.foxprox.network.proxy.networking.protocol.packet.Title packet = new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.TITLE );
            title = new TitlePacketHolder<>( packet, packet );
        }

        title.oldPacket.setText( ComponentSerializer.toString( text ) ); // = newPacket
        return this;
    }

    @Override
    public Title title(BaseComponent... text)
    {
        if ( title == null )
        {
            com.foxprox.network.proxy.networking.protocol.packet.Title packet = new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.TITLE );
            title = new TitlePacketHolder<>( packet, packet );
        }

        title.oldPacket.setText( ComponentSerializer.toString( text ) ); // = newPacket
        return this;
    }

    @Override
    public Title subTitle(BaseComponent text)
    {
        if ( subtitle == null )
        {
            subtitle = new TitlePacketHolder<>( new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.SUBTITLE ), new Subtitle() );
        }

        String serialized = ComponentSerializer.toString( text );
        subtitle.oldPacket.setText( serialized );
        subtitle.newPacket.setText( serialized );
        return this;
    }

    @Override
    public Title subTitle(BaseComponent... text)
    {
        if ( subtitle == null )
        {
            subtitle = new TitlePacketHolder<>( new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.SUBTITLE ), new Subtitle() );
        }

        String serialized = ComponentSerializer.toString( text );
        subtitle.oldPacket.setText( serialized );
        subtitle.newPacket.setText( serialized );
        return this;
    }

    @Override
    public Title fadeIn(int ticks)
    {
        if ( times == null )
        {
            times = createAnimationPacket();
        }

        times.oldPacket.setFadeIn( ticks );
        times.newPacket.setFadeIn( ticks );
        return this;
    }

    @Override
    public Title stay(int ticks)
    {
        if ( times == null )
        {
            times = createAnimationPacket();
        }

        times.oldPacket.setStay( ticks );
        times.newPacket.setStay( ticks );
        return this;
    }

    @Override
    public Title fadeOut(int ticks)
    {
        if ( times == null )
        {
            times = createAnimationPacket();
        }

        times.oldPacket.setFadeOut( ticks );
        times.newPacket.setFadeOut( ticks );
        return this;
    }

    @Override
    public Title clear()
    {
        if ( clear == null )
        {
            clear = new TitlePacketHolder<>( new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.CLEAR ), new ClearTitles() );
        }

        title = null; // No need to send title if we clear it after that again

        return this;
    }

    @Override
    public Title reset()
    {
        if ( reset == null )
        {
            reset = new TitlePacketHolder<>( new com.foxprox.network.proxy.networking.protocol.packet.Title( com.foxprox.network.proxy.networking.protocol.packet.Title.Action.RESET ), new ClearTitles( true ) );
        }

        // No need to send these packets if we reset them later
        title = null;
        subtitle = null;
        times = null;

        return this;
    }

    private static void sendPacket(FoxPlayer player, TitlePacketHolder packet)
    {
        if ( packet != null )
        {
            if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_17 )
            {
                player.unsafe().sendPacket( packet.newPacket );
            } else
            {
                player.unsafe().sendPacket( packet.oldPacket );
            }
        }
    }

    @Override
    public Title send(FoxPlayer player)
    {
        if ( ProtocolConstants.isBeforeOrEq( player.getPendingConnection().getVersion(), ProtocolConstants.MINECRAFT_1_7_6 ) ) return this; // FlameCord - 1.7.x support
        sendPacket( player, clear );
        sendPacket( player, reset );
        sendPacket( player, times );
        sendPacket( player, subtitle );
        sendPacket( player, title );
        return this;
    }
}
