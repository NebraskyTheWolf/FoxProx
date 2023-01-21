package com.foxprox.network.command.foxprox.mod.ingame;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;


public class CommandCheck extends AbstractCommand {
    public CommandCheck() {
        super(FoxServer.getInstance(), "check");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }


    }
}
