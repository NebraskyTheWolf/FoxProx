package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.event.ChatEvent;
import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.samagames.api.channels.JsonModMessage;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.google.gson.Gson;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;
import com.foxprox.network.util.ModerationUtils;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class ModerationListener implements Listener {

    public static final String PROXY_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.DARK_PURPLE + "Staff Chat" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    @EventHandler
    public void onMessage(PubSubMessage message) {
        if (message.getChannel().equals("moderationchan")) {
            JsonModMessage mod = new Gson().fromJson(message.getMessage(), JsonModMessage.class);

            ModerationUtils.sendMessage(mod.getModChannel(), mod.getMessage(), UUID.randomUUID());
        }
    }

    @EventHandler
    public void onChatMessage(ChatEvent event) {
        FoxPlayer player = (FoxPlayer) event.getSender();
        String key = "currentchannel:" + player.getUniqueId().toString();
        Jedis redis = FoxServer.getInstance().getRedisConnection();

        if (redis.exists(key)) {
            if ("moderator".equals(redis.get(key))) {
                if (!event.isProxyCommand() && !event.isCommand()) {
                    ModerationUtils.sendMessage(ModChannel.DISCUSSION, PROXY_TAG + ChatColor.GOLD + "" + ChatColor.BOLD + player.getName() + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " ≫ " + ChatColor.YELLOW + event.getMessage(), UUID.randomUUID());
                    event.setCancelled(true);
                }
            }
        }
    }

}
