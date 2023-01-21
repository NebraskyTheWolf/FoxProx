package com.foxprox.network.command.foxprox.admin;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;

public class CommandSEnd extends AbstractCommand {
    public CommandSEnd() {
        super(FoxServer.getInstance(), "send");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0)
                return;
            FoxServer.getInstance().getPubSub().send("commands.servers." + args[0], "stop");
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }
}
