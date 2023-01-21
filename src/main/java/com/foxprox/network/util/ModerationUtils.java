package com.foxprox.network.util;

import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.ChatMessageType;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ModerationUtils {
    private static final String MODERATING_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;
    public static final String TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Moderation" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;
    public static final String REPORT_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Report" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    public static void sendMessage(ModChannel channel, String message, UUID except) {
        // Formatting the message before sending it to the moderators.
        TextComponent component = (TextComponent) new TextComponent()
                .setText(String.format(MODERATING_TAG + "%s", channel.getName(), message))
                .setColor(channel.getColor());
        // Get online moderators and then sending the message.
        getPlayerFor(channel).forEach((player, modChannel) -> {
                if (player.isConnected())
                    if (!player.getUniqueId().equals(except))
                        player.sendMessage(ChatMessageType.SYSTEM, component);
        });
    }
    // Moderation channels permissions sorter
    private static Map<FoxPlayer, ModChannel> getPlayerFor(ModChannel channel) {
        Map<FoxPlayer, ModChannel> PLAYERS = new LinkedHashMap<>();
        for (FoxPlayer player : FoxServer.getInstance().getPlayers()) {
            switch (channel) {
                case SANCTION:
                    if (player.getPermission().getModerationPermissionsBean().isModBan()
                            || player.getPermission().getModerationPermissionsBean().isModMute()
                            || player.getPermission().getModerationPermissionsBean().isModKick()
                            || player.getPermission().getModerationPermissionsBean().isModMuteLongtime()
                            || player.getPermission().getModerationPermissionsBean().isModPardon())
                        PLAYERS.put(player, ModChannel.SANCTION);
                    break;
                case REPORT:
                    if (player.getPermission().getModerationPermissionsBean().isModChannelReport())
                        PLAYERS.put(player, ModChannel.REPORT);
                    break;
                case DISCUSSION:
                    if (player.getPermission().getModerationPermissionsBean().isModChannel())
                        PLAYERS.put(player, ModChannel.DISCUSSION);
                    break;
                case INFORMATION:
                    if (player.getPermission().getModerationPermissionsBean().isModChannel())
                        PLAYERS.put(player, ModChannel.INFORMATION);
                    break;
            }
        }
        return PLAYERS;
    }
}
