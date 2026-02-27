package cn.tj.food.test;

import cn.tj.food.common.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskTest {
    private static final TaskPool taskPool = TaskPool.getInstance();

    public static void main(String[] args) throws Exception {
        String monitorTaskName = "monitor-task";
        taskPool.addScheduledTask(
                monitorTaskName,
                new MonitorProcessor(),
                1000
        );
        taskPool.list();
        Thread.sleep(1000*10);
        ScheduledTask monitorTask = (ScheduledTask) taskPool.getTask(monitorTaskName);
        taskPool.addGeneralTask(
                "shutdown-task",
                new SimpleProcessor() {
                    @Override
                    public void process() throws Exception {
                        monitorTask.deactivate();
                    }
                }
        );
        Thread.sleep(1000*5);
        taskPool.list();
    }

    private static class MonitorProcessor extends SimpleProcessor {
        private static final Logger logger = LoggerFactory.getLogger(MonitorProcessor.class);

        @Override
        public void process() throws Exception {
            logger.info("hello");
        }
    }
}