package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.event.EventHandler;
import com.foxprox.network.util.ModerationUtils;

import java.util.UUID;

public class BanListeners implements Listener {
    private static final String TAG = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "[" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Samaritan" + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "] ";

    @EventHandler
    public void onBan(PubSubMessage message) {
        if (message.getChannel().equals("cheat")) {
            String[] args = message.getMessage().split("#####");

            ModerationUtils.sendMessage(ModChannel.REPORT, TAG +  "The player " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + args[0] + ChatColor.DARK_PURPLE + ChatColor.BOLD + " has been banned for the reason " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + args[1] + ChatColor.DARK_PURPLE + ChatColor.BOLD + ".", UUID.randomUUID());
        }
    }
}
