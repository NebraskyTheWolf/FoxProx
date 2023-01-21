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
import java.time.format.DateTimeFormatter;

public class CommandMute extends AbstractCommand {
    public CommandMute() {
        super(FoxServer.getInstance(), "mute");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws Exception {
        if (!hasPermission(sender, "mod.mute")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        if (args.length < 2) {
            this.sendError(sender, "Usage: /mute <playerName> <reasons>");
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
        FoxPlayer moderator = (FoxPlayer)sender;

        SanctionBean documents = new SanctionBean(
                target.getUniqueId(),
                SanctionBean.MUTE,
                reason,
                moderator.getUniqueId(),
                new Timestamp(System.currentTimeMillis() + 380000),
                false
        );

        if (target.isConnected()) {
            FoxServer.getInstance()
                    .getGameServiceManager()
                    .applySanction(documents.getTypeId(), documents);

            this.sendSuccess(sender, "You muted " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason);
            this.sendWarning(target, "You got muted for " + ChatColor.GOLD + reason + ChatColor.YELLOW + " until " + ChatColor.RED + documents.getExpirationTime().toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            ModerationUtils.sendMessage(ModChannel.SANCTION, ChatColor.GOLD + "" + moderator.getName() + ChatColor.YELLOW + " Muted " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason + ChatColor.YELLOW + ".", moderator.getUniqueId());
        } else {
            this.sendError(sender, "Player " + ChatColor.GOLD + target + ChatColor.YELLOW + " specified is disconnected from the network.");
        }

    }
}
