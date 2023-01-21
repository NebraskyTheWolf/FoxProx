package com.foxprox.network.command.foxprox.mod.ingame;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;


public class CommandSTP extends AbstractCommand {
    public CommandSTP() {
        super(FoxServer.getInstance(), "stp");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }
    }
}
