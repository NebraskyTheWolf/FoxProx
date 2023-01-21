package com.foxprox.network.command;

import com.foxprox.network.command.AbstractCommand;
import com.foxprox.network.proxy.core.api.CommandSender;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.plugin.TabExecutor;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @deprecated internal use only
 */
@Deprecated
public abstract class PlayerCommand extends AbstractCommand implements TabExecutor
{

    public PlayerCommand(String name)
    {
        super(FoxServer.getInstance(), name );
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase( Locale.ROOT ) : "";
        return FoxServer.getInstance().getPlayers().stream().filter(player -> player.getName().toLowerCase(Locale.ROOT).startsWith(lastArg)).collect(Collectors.toList()).stream().map(player -> player.getName()).collect(Collectors.toList());
    }
}
