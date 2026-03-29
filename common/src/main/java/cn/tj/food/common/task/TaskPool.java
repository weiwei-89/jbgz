package cn.tj.food.common.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskPool {
    private static final Logger logger = LoggerFactory.getLogger(TaskPool.class);
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Map<String, CommonTask> taskMap = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(
            AVAILABLE_PROCESSORS*2,
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName(String.format("task-pool-%s", this.count.getAndIncrement()));
                    t.setDaemon(true);
                    return t;
                }
            });

    private TaskPool() {

    }

    private static class SingletonHolder {
        private static final TaskPool INSTANCE = new TaskPool();
    }

    public static TaskPool getInstance() {
        return TaskPool.SingletonHolder.INSTANCE;
    }

    public ScheduledExecutorService getPool() {
        return this.pool;
    }

    public void addGeneralTask(String taskName, Processor processor) throws Exception {
        this.addTask(
                taskName,
                new GeneralTaskProcessor(
                        new DefaultGeneralTask(processor)
                )
        );
    }

    public void addGeneralTask(String taskName, GeneralTask task) throws Exception {
        this.addTask(
                taskName,
                new GeneralTaskProcessor(task)
        );
    }

    public void addScheduledTask(String taskName, Processor processor, long interval) throws Exception {
        this.addTask(
                taskName,
                new ScheduledTaskProcessor(
                        new DefaultScheduledTask(
                                processor,
                                interval
                        )
                )
        );
    }

    public void addScheduledTask(String taskName, ScheduledTask task) throws Exception {
        this.addTask(
                taskName,
                new ScheduledTaskProcessor(task)
        );
    }

    private void addTask(String taskName, TaskProcessor<?> processor) throws Exception {
        if(this.taskMap.containsKey(taskName)) {
            logger.info("task exists [{}]", taskName);
            return;
        }
        boolean acquired = this.rwLock.writeLock()
                .tryLock(10, TimeUnit.SECONDS);
        if(!acquired) {
            logger.info("task added failed [{}]", taskName);
            return;
        }
        try {
            if(this.taskMap.containsKey(taskName)) {
                logger.info("task exists [{}]", taskName);
                return;
            }
            this.pool.submit(
                    new GeneralTask(processor) {
                        @Override
                        protected void done(boolean result) {
                            rwLock.writeLock().lock();
                            try {
                                taskMap.remove(taskName);
                                logger.info("task removed [{}]", taskName);
                            } finally {
                                rwLock.writeLock().unlock();
                            }
                        }
                    }
            );
            this.taskMap.put(taskName, processor.getTask());
            logger.info("task added [{}]", taskName);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public CommonTask getTask(String taskName) throws Exception {
        if(!this.taskMap.containsKey(taskName)) {
            logger.info("task does not exist [{}]", taskName);
            return null;
        }
        boolean acquired = this.rwLock.readLock()
                .tryLock(10, TimeUnit.SECONDS);
        if(!acquired) {
            logger.info("task list acquired failed");
            return null;
        }
        try {
            if(!this.taskMap.containsKey(taskName)) {
                logger.info("task does not exist [{}]", taskName);
                return null;
            }
            return this.taskMap.get(taskName);
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

//    public void stopTask(String taskName) throws Exception {
//        if(!this.taskMap.containsKey(taskName)) {
//            logger.info("task does not exist [{}]", taskName);
//            return;
//        }
//        boolean acquired = this.rwLock.writeLock()
//                .tryLock(10, TimeUnit.SECONDS);
//        if(!acquired) {
//            logger.info("task stopped failed [{}]", taskName);
//            return;
//        }
//        try {
//            if(!this.taskMap.containsKey(taskName)) {
//                logger.info("task does not exist [{}]", taskName);
//                return;
//            }
//            CommonTask task = this.taskMap.get(taskName);
//            task.stop();
//        } finally {
//            this.rwLock.writeLock().unlock();
//        }
//    }

    public void list() throws Exception {
        if(this.taskMap.isEmpty()) {
            logger.info("there are no tasks");
            return;
        }
        boolean acquired = this.rwLock.readLock()
                .tryLock(10, TimeUnit.SECONDS);
        if(!acquired) {
            logger.info("task list acquired failed");
            return;
        }
        try {
            if(this.taskMap.isEmpty()) {
                logger.info("there are no tasks");
                return;
            }
            logger.info("there are {} tasks", this.taskMap.size());
            int number = 0;
            for(Map.Entry<String, CommonTask> entry : this.taskMap.entrySet()) {
                number++;
                String taskName = entry.getKey();
                logger.info("{}.{}", number, taskName);
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    private abstract class TaskProcessor<T extends CommonTask> extends SimpleProcessor {
        private final T task;

        public TaskProcessor(T task) {
            this.task = task;
        }

        public T getTask() {
            return this.task;
        }
    }

    private class GeneralTaskProcessor extends TaskProcessor<GeneralTask> {
        public GeneralTaskProcessor(GeneralTask task) {
            super(task);
        }

        @Override
        public void process() throws Exception {
            this.getTask().run();
        }
    }

    private class ScheduledTaskProcessor extends TaskProcessor<ScheduledTask> {
        public ScheduledTaskProcessor(ScheduledTask task) {
            super(task);
        }

        @Override
        public void process() throws Exception {
            Thread.sleep(this.getTask().getInterval());
            this.getTask().run();
            this.getTask().waitForDeactivation();
        }
    }

    private class DefaultGeneralTask extends GeneralTask {
        public DefaultGeneralTask(Processor processor) {
            super(processor);
        }
    }

    private class DefaultScheduledTask extends ScheduledTask {
        public DefaultScheduledTask(
                Processor processor,
                long interval
        ) {
            super(processor, pool, interval);
        }

        @Override
        protected void beforeNext(boolean result) throws Exception {

        }
    }
}