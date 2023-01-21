package com.foxprox.network.command;

import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "foxprox" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.RED + "---------------------------------------------");
        sender.sendMessage(ChatColor.RED +"FoxProx v" + FoxServer.getInstance().getVersion());
        sender.sendMessage(ChatColor.RED +"By Vakea using a fork of Waterfall ( Private Fork )");
        sender.sendMessage(ChatColor.RED +"---------------------------------------------");
    }
}
