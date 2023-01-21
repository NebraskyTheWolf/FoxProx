package com.foxprox.network.proxy.core.api.scheduler;

import java.util.concurrent.ThreadFactory;
import lombok.Data;
import com.foxprox.network.proxy.core.api.plugin.Plugin;

@Data
public class GroupedThreadFactory implements ThreadFactory
{

    private final ThreadGroup group;

    public static final class FoxGroup extends ThreadGroup
    {

        private FoxGroup(String name)
        {
            super( name );
        }

    }

    public GroupedThreadFactory(Plugin plugin, String name)
    {
        this.group = new FoxGroup( name );
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return new Thread( group, r );
    }
}
