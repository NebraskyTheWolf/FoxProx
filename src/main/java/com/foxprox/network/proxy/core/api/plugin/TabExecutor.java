package com.foxprox.network.proxy.core.api.plugin;

import com.foxprox.network.proxy.core.api.CommandSender;

public interface TabExecutor
{

    public Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
