package com.foxprox.network.proxy.core.api.plugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PluginLogger extends Logger
{

    private final String pluginName;

    protected PluginLogger(Plugin plugin)
    {
        super( plugin.getClass().getCanonicalName(), null );
        pluginName = "[" + plugin.getDescription().getName() + "] ";
    }

    @Override
    public void log(LogRecord logRecord)
    {
        logRecord.setMessage( pluginName + logRecord.getMessage() );
        super.log( logRecord );
    }
}
