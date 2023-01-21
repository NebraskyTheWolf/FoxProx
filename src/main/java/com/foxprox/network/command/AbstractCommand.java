package com.foxprox.network.command;

import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.permissions.EntityPermissions;
import com.foxprox.network.proxy.core.api.plugin.Command;

import java.util.List;

public abstract class AbstractCommand extends Command {

    protected final FoxServer plugin;

    public static final String TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "FoxGuard" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    public AbstractCommand(FoxServer plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) throws Exception {
        if (sender instanceof FoxPlayer) {
            this.onCommand(sender, args);
        } else {
            this.sendError(sender, "You can't use this command in the console.");
            this.sendError(sender, "The developer console is set on `SAFE` mode.");
            this.sendError(sender, "---");
        }
    }

    protected abstract void onCommand(CommandSender sender, String[] args) throws Exception;

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.RED + "" + ChatColor.BOLD + "✖" + ChatColor.YELLOW + " " + message));
    }

    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.GREEN + "" + ChatColor.BOLD + "✔" + ChatColor.YELLOW + " " + message));
    }

    protected void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.GOLD + "" + ChatColor.BOLD + "⚠" + ChatColor.YELLOW + " " + message));
    }

    protected void kickTarget(FoxPlayer sender, String reason) {
        sender.disconnect(new TextComponent(TAG + ChatColor.RED + "" + ChatColor.BOLD + "✖ " + reason));
    }

    protected void sendFormattedList(CommandSender sender, String TAG, List<String> messages) {
        StringBuilder formatted = new StringBuilder(TAG + "\n");
        for (String str : messages)
            formatted.append(ChatColor.GOLD).append("├").append(" ").append(str).append("\n");
        sender.sendMessage(new TextComponent(formatted.toString()));
    }

    protected EntityPermissions getSenderPermissions(CommandSender sender) {
        FoxPlayer player = (FoxPlayer) sender;
        return player.getPermission();
    }

    protected boolean hasPermission(CommandSender sender, String permissions) {
        FoxPlayer player = (FoxPlayer) sender;
        return player.getPermission().getListPermissions().get(permissions);
    }
}
