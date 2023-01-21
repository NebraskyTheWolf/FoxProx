package com.foxprox.network.command.foxprox.admin;


import com.foxprox.network.FoxProx;
import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.TabExecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandServer extends AbstractCommand implements TabExecutor {
    public CommandServer() {
        super(FoxServer.getInstance(), "server");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        FoxPlayer player = (FoxPlayer) sender;

        if (args.length == 0)
            sender.sendMessage(new TextComponent(FoxProx.PROXY_TAG + ChatColor.RED + "Missing arguments /server <server_name>"));
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandServer()) {
            ServerInfo info = FoxServer.getInstance().getServerInfo(args[0]);
            if (info.canAccess(sender))
                if (player.isConnected())
                    player.connect(info);
            else player.sendMessage(new TextComponent(FoxProx.PROXY_TAG + ChatColor.RED + "Unable to connect on " + args[0]));
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> SERVERS = new CopyOnWriteArrayList<>();
        for (ServerInfo info : FoxProx.getInstance().getServers().values()) {
            if (!SERVERS.contains(info.getName()))
                SERVERS.add(info.getName());
        }
        return SERVERS;
    }
}
