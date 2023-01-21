package com.foxprox.network.command.foxprox.admin;

import com.foxprox.network.FoxProx;
import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;

public class CommandEnd extends AbstractCommand {
    public CommandEnd()
    {
        super(FoxServer.getInstance(), "end" );
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0) {
                FoxServer.getInstance().stop(FoxProx.PROXY_TAG + "Shutting down....");
            } else {
                FoxServer.getInstance().stop(FoxProx.PROXY_TAG + "Requested by administrator." );
            }
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }
}
