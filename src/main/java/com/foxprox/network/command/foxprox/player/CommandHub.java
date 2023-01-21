package com.foxprox.network.command.foxprox.player;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;

public class CommandHub extends AbstractCommand {
    public CommandHub() {
        super(FoxServer.getInstance(), "lobby");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        FoxPlayer player = (FoxPlayer)sender;
        ServerInfo lobby = FoxServer.getInstance().getLobbyManager().fetchRandomHub();
        if (lobby.canAccess(sender))
            player.connect(lobby);
        else
            this.sendError(sender, "Unable to access to the lobby #" + lobby.getName().split("_")[1]);
    }
}
