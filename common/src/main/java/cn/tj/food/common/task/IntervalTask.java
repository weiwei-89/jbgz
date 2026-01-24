package cn.tj.food.common.task;

public class IntervalTask extends CommonTask {
    private final long interval;

    public IntervalTask(Processor processor, long interval) {
        super(processor);
        this.interval = interval;
    }

    public long getInterval() {
        return this.interval;
    }

    private long lastProcessTime = System.currentTimeMillis();

    public long getLastProcessTime() {
        return this.lastProcessTime;
    }

    @Override
    protected boolean trigger() {
        if(System.currentTimeMillis() >= this.lastProcessTime+this.interval) {
            return true;
        }
        return false;
    }

    @Override
    protected void done(boolean result) {
        this.lastProcessTime = System.currentTimeMillis();
    }
}