package cn.tj.food.common.task;

public class GeneralTask extends CommonTask {
    public GeneralTask(Processor processor) {
        super(processor);
    }

    @Override
    protected boolean trigger() {
        return true;
    }
}