package com.foxprox.network.command.foxprox.mod.chat;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import redis.clients.jedis.Jedis;


public class CommandChannel extends AbstractCommand {
    public CommandChannel() {
        super(FoxServer.getInstance(), "channel");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.channel")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        FoxPlayer player = (FoxPlayer) sender;

        Jedis redis = FoxServer.getInstance().getRedisConnection();
        String key = "currentchannel:" + player.getUniqueId().toString();

        String currentChannel = redis.get(key);
        if (currentChannel == null) {
            redis.set(key, "moderator");
            this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.RED + "moderator" + ChatColor.DARK_BLUE + " chat.");
        } else {
            if (currentChannel.equals("moderator")) {
                redis.set(key, "public");
                this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.GREEN + "public" + ChatColor.DARK_BLUE + " chat.");
            } else if (currentChannel.equals("public")) {
                redis.set(key, "moderator");
                this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.RED + "moderator" + ChatColor.DARK_BLUE + " chat.");
            }
        }
        redis.close();
    }
}
