package cn.tj.food.test;

import cn.tj.food.common.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskTest {
    private static final TaskPool taskPool = TaskPool.getInstance();
    private static final ScheduledTaskPool scheduledTaskPool = ScheduledTaskPool.getInstance();

    public static void main(String[] args) throws Exception {
        ScheduledTask scheduledTask = new ScheduledTask(
                new MonitorProcessor(),
                scheduledTaskPool.getPool(),
                1000
        ) {

        };
        taskPool.addScheduledTask(
                "monitor-task",
                scheduledTask
        );
        taskPool.list();
        Thread.sleep(1000*10);
        taskPool.addTask(
                "shutdown-task",
                new GeneralTask(
                        new Processor() {
                            @Override
                            public void init() throws Exception {

                            }

                            @Override
                            public void process() throws Exception {
                                scheduledTask.deactivate();
                            }
                        }
                ) {

                }
        );
        Thread.sleep(1000*5);
        taskPool.list();
        Thread.sleep(1000*3600);
    }

    private static class MonitorProcessor implements Processor {
        private static final Logger logger = LoggerFactory.getLogger(MonitorProcessor.class);

        @Override
        public void init() throws Exception {

        }

        @Override
        public void process() throws Exception {
            logger.info("hello");
        }
    }
}