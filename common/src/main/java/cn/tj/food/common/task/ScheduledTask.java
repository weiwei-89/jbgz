package cn.tj.food.common.task;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTask extends CommonTask {
    private final ScheduledExecutorService pool;
    private final long interval;

    public ScheduledTask(
            Processor processor,
            ScheduledExecutorService pool,
            long interval
    ) {
        super(processor);
        this.pool = pool;
        this.interval = interval;
    }

    public long getInterval() {
        return this.interval;
    }

    private volatile boolean activated = true;
    private long lastProcessTime = System.currentTimeMillis();
    private ScheduledFuture<?> schedule;

    public boolean isActivated() {
        return this.activated;
    }

    public synchronized void waitForDeactivation() throws InterruptedException {
        while(this.activated) {
            this.wait();
        }
    }

    public synchronized void deactivate() {
        this.activated = false;
        this.notifyAll();
        if(this.schedule != null) {
            this.schedule.cancel(false);
            this.schedule = null;
        }
    }

    @Override
    protected boolean trigger() {
        return true;
    }

    @Override
    protected final synchronized void done(boolean result) {
        try {
            this.beforeNext(result);
        } catch(Exception e) {
            this.error(e);
        }
        if(!this.activated) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - this.lastProcessTime;
        if(timeElapsed >= this.interval) {
            this.lastProcessTime = currentTime;
            this.schedule = this.pool.schedule(
                    this,
                    this.interval,
                    TimeUnit.MILLISECONDS
            );
        } else {
            this.schedule = this.pool.schedule(
                    this,
                    this.interval-timeElapsed,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    protected abstract void beforeNext(boolean result) throws Exception;
}