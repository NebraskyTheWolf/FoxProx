package com.foxprox.network.command.foxprox.mod.chat;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;

public class CommandSlow extends AbstractCommand {
    public CommandSlow() {
        super(FoxServer.getInstance(), "slow");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }


    }
}
