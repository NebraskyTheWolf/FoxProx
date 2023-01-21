package com.foxprox.network.proxy.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.foxprox.logger.api.Level;
import com.foxprox.network.proxy.core.api.plugin.Plugin;
import com.foxprox.network.proxy.core.api.scheduler.ScheduledTask;
import com.foxprox.network.proxy.lib.event.ProxyExceptionEvent;
import com.foxprox.network.proxy.lib.exception.ProxySchedulerException;
import lombok.Data;
import com.foxprox.network.proxy.core.api.FoxServer;

@Data
public class FoxTask implements Runnable, ScheduledTask
{

    private final FoxScheduler sched;
    private final int id;
    private final Plugin owner;
    private final Runnable task;
    //
    private final long delay;
    private final long period;
    private final AtomicBoolean running = new AtomicBoolean( true );

    public FoxTask(FoxScheduler sched, int id, Plugin owner, Runnable task, long delay, long period, TimeUnit unit)
    {
        this.sched = sched;
        this.id = id;
        this.owner = owner;
        this.task = task;
        this.delay = unit.toMillis( delay );
        this.period = unit.toMillis( period );
    }

    @Override
    public void cancel()
    {
        boolean wasRunning = running.getAndSet( false );

        if ( wasRunning )
        {
            sched.cancel0( this );
        }
    }

    @Override
    public void run()
    {
        if ( delay > 0 )
        {
            try
            {
                Thread.sleep( delay );
            } catch ( InterruptedException ex )
            {
                Thread.currentThread().interrupt();
            }
        }

        while ( running.get() )
        {
            try
            {
                task.run();
            } catch ( Throwable t )
            {
                //Waterfall start - throw exception event
                String msg = String.format( "Task %s encountered an exception", this );
                FoxServer.getInstance().getLogger().log( Level.SEVERE, msg, t );
                FoxServer.getInstance().getPluginManager().callEvent( new ProxyExceptionEvent( new ProxySchedulerException( msg, t, this ) ) );
                //Waterfall end
            }

            // If we have a period of 0 or less, only run once
            if ( period <= 0 )
            {
                break;
            }

            try
            {
                Thread.sleep( period );
            } catch ( InterruptedException ex )
            {
                Thread.currentThread().interrupt();
            }
        }

        cancel();
    }
}
