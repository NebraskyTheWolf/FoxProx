package com.foxprox.network.proxy.networking.protocol;

import com.foxprox.network.proxy.networking.protocol.packet.BossBar;
import com.foxprox.network.proxy.networking.protocol.packet.Chat;
import com.foxprox.network.proxy.networking.protocol.packet.ClearTitles;
import com.foxprox.network.proxy.networking.protocol.packet.ClientChat;
import com.foxprox.network.proxy.networking.protocol.packet.ClientCommand;
import com.foxprox.network.proxy.networking.protocol.packet.ClientSettings;
import com.foxprox.network.proxy.networking.protocol.packet.ClientStatus;
import com.foxprox.network.proxy.networking.protocol.packet.Commands;
import com.foxprox.network.proxy.networking.protocol.packet.EncryptionRequest;
import com.foxprox.network.proxy.networking.protocol.packet.EncryptionResponse;
import com.foxprox.network.proxy.networking.protocol.packet.EntityStatus;
import com.foxprox.network.proxy.networking.protocol.packet.GameState;
import com.foxprox.network.proxy.networking.protocol.packet.Handshake;
import com.foxprox.network.proxy.networking.protocol.packet.KeepAlive;
import com.foxprox.network.proxy.networking.protocol.packet.Kick;
import com.foxprox.network.proxy.networking.protocol.packet.LegacyHandshake;
import com.foxprox.network.proxy.networking.protocol.packet.LegacyPing;
import com.foxprox.network.proxy.networking.protocol.packet.Login;
import com.foxprox.network.proxy.networking.protocol.packet.LoginPayloadRequest;
import com.foxprox.network.proxy.networking.protocol.packet.LoginPayloadResponse;
import com.foxprox.network.proxy.networking.protocol.packet.LoginRequest;
import com.foxprox.network.proxy.networking.protocol.packet.LoginSuccess;
import com.foxprox.network.proxy.networking.protocol.packet.PingPacket;
import com.foxprox.network.proxy.networking.protocol.packet.PlayerListHeaderFooter;
import com.foxprox.network.proxy.networking.protocol.packet.PlayerListItem;
import com.foxprox.network.proxy.networking.protocol.packet.PluginMessage;
import com.foxprox.network.proxy.networking.protocol.packet.Respawn;
import com.foxprox.network.proxy.networking.protocol.packet.ScoreboardDisplay;
import com.foxprox.network.proxy.networking.protocol.packet.ScoreboardObjective;
import com.foxprox.network.proxy.networking.protocol.packet.ScoreboardScore;
import com.foxprox.network.proxy.networking.protocol.packet.SetCompression;
import com.foxprox.network.proxy.networking.protocol.packet.StatusRequest;
import com.foxprox.network.proxy.networking.protocol.packet.StatusResponse;
import com.foxprox.network.proxy.networking.protocol.packet.Subtitle;
import com.foxprox.network.proxy.networking.protocol.packet.SystemChat;
import com.foxprox.network.proxy.networking.protocol.packet.TabCompleteRequest;
import com.foxprox.network.proxy.networking.protocol.packet.TabCompleteResponse;
import com.foxprox.network.proxy.networking.protocol.packet.Team;
import com.foxprox.network.proxy.networking.protocol.packet.Title;
import com.foxprox.network.proxy.networking.protocol.packet.TitleTimes;
import com.foxprox.network.proxy.networking.protocol.packet.ViewDistance;

public abstract class AbstractPacketHandler
{

    public void handle(LegacyPing ping) throws Exception
    {
    }

    public void handle(TabCompleteResponse tabResponse) throws Exception
    {
    }

    public void handle(PingPacket ping) throws Exception
    {
    }

    public void handle(StatusRequest statusRequest) throws Exception
    {
    }

    public void handle(StatusResponse statusResponse) throws Exception
    {
    }

    public void handle(Handshake handshake) throws Exception
    {
    }

    public void handle(KeepAlive keepAlive) throws Exception
    {
    }

    public void handle(Login login) throws Exception
    {
    }

    public void handle(Chat chat) throws Exception
    {
    }

    public void handle(ClientChat chat) throws Exception
    {
    }

    public void handle(SystemChat chat) throws Exception
    {
    }

    public void handle(ClientCommand command) throws Exception
    {
    }

    public void handle(Respawn respawn) throws Exception
    {
    }

    public void handle(LoginRequest loginRequest) throws Exception
    {
    }

    public void handle(ClientSettings settings) throws Exception
    {
    }

    public void handle(ClientStatus clientStatus) throws Exception
    {
    }

    public void handle(PlayerListItem playerListItem) throws Exception
    {
    }

    public void handle(PlayerListHeaderFooter playerListHeaderFooter) throws Exception
    {
    }

    public void handle(TabCompleteRequest tabComplete) throws Exception
    {
    }

    public void handle(ScoreboardObjective scoreboardObjective) throws Exception
    {
    }

    public void handle(ScoreboardScore scoreboardScore) throws Exception
    {
    }

    public void handle(EncryptionRequest encryptionRequest) throws Exception
    {
    }

    public void handle(ScoreboardDisplay displayScoreboard) throws Exception
    {
    }

    public void handle(Team team) throws Exception
    {
    }

    public void handle(Title title) throws Exception
    {
    }

    public void handle(Subtitle title) throws Exception
    {
    }

    public void handle(TitleTimes title) throws Exception
    {
    }

    public void handle(ClearTitles title) throws Exception
    {
    }

    public void handle(PluginMessage pluginMessage) throws Exception
    {
    }

    public void handle(Kick kick) throws Exception
    {
    }

    public void handle(EncryptionResponse encryptionResponse) throws Exception
    {
    }

    public void handle(LoginSuccess loginSuccess) throws Exception
    {
    }

    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
    }

    public void handle(SetCompression setCompression) throws Exception
    {
    }

    public void handle(BossBar bossBar) throws Exception
    {
    }

    public void handle(LoginPayloadRequest request) throws Exception
    {
    }

    public void handle(LoginPayloadResponse response) throws Exception
    {
    }

    public void handle(EntityStatus status) throws Exception
    {
    }

    public void handle(Commands commands) throws Exception
    {
    }

    public void handle(ViewDistance viewDistance) throws Exception
    {
    }

    public void handle(GameState gameState) throws Exception
    {
    }
}
