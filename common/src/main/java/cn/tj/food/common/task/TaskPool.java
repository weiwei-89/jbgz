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

    private final Map<String, Future<?>> taskMap = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(
            AVAILABLE_PROCESSORS,
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

    public void addTask(String taskName, GeneralTask task) throws Exception {
        this.addTask(taskName, new GeneralTaskProcessor(task));
    }

    public void addScheduledTask(String taskName, ScheduledTask task) throws Exception {
        this.addTask(taskName, new ScheduledTaskProcessor(task));
    }

    public void addTask(String taskName, Processor processor) throws Exception {
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
            Future<?> future = this.pool.submit(
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
            this.taskMap.put(taskName, future);
            logger.info("task added [{}]", taskName);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    private class GeneralTaskProcessor implements Processor {
        private final GeneralTask task;

        public GeneralTaskProcessor(GeneralTask task) {
            this.task = task;
        }

        @Override
        public void init() throws Exception {

        }

        @Override
        public void process() throws Exception {
            this.task.run();
        }
    }

    private class ScheduledTaskProcessor implements Processor {
        private final ScheduledTask task;

        public ScheduledTaskProcessor(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void init() throws Exception {

        }

        @Override
        public void process() throws Exception {
            this.task.run();
            this.task.waitForDeactivation();
        }
    }

    public void stopTask(String taskName) throws Exception {
        if(!this.taskMap.containsKey(taskName)) {
            logger.info("task does not exist [{}]", taskName);
            return;
        }
        boolean acquired = this.rwLock.readLock()
                .tryLock(10, TimeUnit.SECONDS);
        if(!acquired) {
            logger.info("task stopped failed [{}]", taskName);
            return;
        }
        try {
            if(!this.taskMap.containsKey(taskName)) {
                logger.info("task does not exist [{}]", taskName);
                return;
            }
            Future<?> future = this.taskMap.get(taskName);
            future.cancel(false);
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

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
            for(Map.Entry<String, Future<?>> entry : this.taskMap.entrySet()) {
                number++;
                String taskName = entry.getKey();
                logger.info("{}.{}", number, taskName);
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }
}