package com.foxprox.network.proxy.scheduler;

import com.foxprox.network.proxy.core.api.plugin.Plugin;
import com.foxprox.network.proxy.core.api.scheduler.ScheduledTask;
import com.foxprox.network.proxy.core.api.scheduler.TaskScheduler;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FoxScheduler implements TaskScheduler
{

    private final Object lock = new Object();
    private final AtomicInteger taskCounter = new AtomicInteger();
    private final TIntObjectMap<FoxTask> tasks = TCollections.synchronizedMap( new TIntObjectHashMap<FoxTask>() );
    private final Multimap<Plugin, FoxTask> tasksByPlugin = Multimaps.synchronizedMultimap( HashMultimap.<Plugin, FoxTask>create() );
    //
    private final Unsafe unsafe = new Unsafe()
    {

        @Override
        public ExecutorService getExecutorService(Plugin plugin)
        {
            return plugin.getExecutorService();
        }
    };

    @Override
    public void cancel(int id)
    {
        FoxTask task = tasks.get( id );
        Preconditions.checkArgument( task != null, "No task with id %s", id );

        task.cancel();
    }

    void cancel0(FoxTask task)
    {
        synchronized ( lock )
        {
            tasks.remove( task.getId() );
            tasksByPlugin.values().remove( task );
        }
    }

    @Override
    public void cancel(ScheduledTask task)
    {
        task.cancel();
    }

    @Override
    public int cancel(Plugin plugin)
    {
        Set<ScheduledTask> toRemove;
        synchronized ( lock )
        {
            toRemove = new HashSet<>(tasksByPlugin.get(plugin));
        }
        for ( ScheduledTask task : toRemove )
        {
            cancel( task );
        }
        return toRemove.size();
    }

    @Override
    public ScheduledTask runAsync(Plugin owner, Runnable task)
    {
        return schedule( owner, task, 0, TimeUnit.MILLISECONDS );
    }

    @Override
    public ScheduledTask schedule(Plugin owner, Runnable task, long delay, TimeUnit unit)
    {
        return schedule( owner, task, delay, 0, unit );
    }

    @Override
    public ScheduledTask schedule(Plugin owner, Runnable task, long delay, long period, TimeUnit unit)
    {
        Preconditions.checkNotNull( owner, "owner" );
        Preconditions.checkNotNull( task, "task" );
        FoxTask prepared = new FoxTask( this, taskCounter.getAndIncrement(), owner, task, delay, period, unit );

        synchronized ( lock )
        {
            tasks.put( prepared.getId(), prepared );
            tasksByPlugin.put( owner, prepared );
        }

        owner.getExecutorService().execute( prepared );
        return prepared;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }
}
