package com.foxprox.network.command.foxprox.mod.sanction;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.util.ModerationUtils;
import com.foxprox.network.util.StringUtils;
import net.samagames.persistanceapi.beans.players.SanctionBean;

import java.sql.Timestamp;

public class CommandBan extends AbstractCommand {
    public CommandBan() {
        super(FoxServer.getInstance(), "ban");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws Exception {
        if (!hasPermission(sender, "mod.ban")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        if (args.length < 2) {
            this.sendError(sender, "Usage: /ban <playerName> <reasons>");
            return;
        }

        String player = args[0];
        String reason = StringUtils.join(args, " ", 1, args.length);

        FoxPlayer target = FoxServer.getInstance().getPlayer(player);
        if (target == null) {
            this.sendError(sender, "Player not found.");
            return;
        }
        if (reason.length() < 1) {
            this.sendError(sender, "You have to set a reasons.");
            return;
        }
        FoxPlayer moderator = (FoxPlayer) sender;

        SanctionBean documents = new SanctionBean(
                target.getUniqueId(),
                SanctionBean.BAN,
                reason,
                moderator.getUniqueId(),
                new Timestamp(System.currentTimeMillis() + 38000000),
                false
        );

        if (target.isConnected()) {
            FoxServer.getInstance()
                    .getGameServiceManager()
                    .applySanction(documents.getTypeId(), documents);

            this.sendSuccess(sender, "You banned " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason);
            this.kickTarget(target, "You are " + ChatColor.RED + " PERMANENTLY " + ChatColor.YELLOW + "banned for " + ChatColor.GOLD + reason + ChatColor.YELLOW + ".");

            ModerationUtils.sendMessage(ModChannel.SANCTION, ChatColor.GOLD + "" + moderator.getName() + ChatColor.YELLOW + " Banned " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason + ChatColor.YELLOW + "( " + ChatColor.RED + "PERMANENTLY" + ChatColor.YELLOW + ").", moderator.getUniqueId());
        } else {
            this.sendError(sender, "Player " + ChatColor.GOLD + target + ChatColor.YELLOW + " specified is disconnected from the network.");
        }
    }
}
