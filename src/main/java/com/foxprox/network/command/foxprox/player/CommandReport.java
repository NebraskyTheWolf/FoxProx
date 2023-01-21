package com.foxprox.network.command.foxprox.player;


import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.util.ModerationUtils;

import java.util.UUID;

public class CommandReport extends AbstractCommand {
    public CommandReport() {
        super(FoxServer.getInstance(), "report");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.sendError(sender, "Usage: /report <playerName> <reason>");
        } else {
            String player = args[0];
            String reason = args[1];

            FoxPlayer target = FoxServer.getInstance().getPlayer(player);

            if (target != null && target.isConnected()) {
                if (reason.length() < 1) {
                    this.sendError(sender, "You need to put a reason.");
                } else {
                    String currentServer = FoxServer.getInstance()
                            .getRedisConnection()
                            .get("currentserver:" + target.getUniqueId().toString());

                    this.sendSuccess(sender, "Player " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " reported to the moderators.");

                    ModerationUtils.sendMessage(ModChannel.REPORT, ChatColor.YELLOW + "Player " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " report on the server " + ChatColor.GOLD + currentServer + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason, UUID.randomUUID());
                }
            } else {
                this.sendError(sender, "The specified player is not connected.");
            }
        }
    }
}
