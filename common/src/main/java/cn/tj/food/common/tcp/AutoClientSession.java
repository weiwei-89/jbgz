package cn.tj.food.common.tcp;

import cn.tj.food.common.task.ScheduledTask;
import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AutoClientSession<CNT> extends ClientSessionAdapter<CNT> implements ReconnectableClientSession<CNT> {
    private final TaskPool taskPool = TaskPool.getInstance();
    private final String RETRY_TASK_NAME = "session-retry-task";

    public AutoClientSession(ClientSession<CNT> session) {
        super(session);
    }

    @Override
    protected void connectDone(Config config) throws Exception {
        this.addListener(config);
    }

    private void addListener(Config config) throws Exception {
        this.taskPool.addScheduledTask(
                RETRY_TASK_NAME,
                new IsActiveProcessor<>(this, config),
                10*1000
        );
    }

    private static class IsActiveProcessor<CNT> extends SimpleProcessor {
        private static final Logger logger = LoggerFactory.getLogger(IsActiveProcessor.class);

        private final ReconnectableClientSession<CNT> session;
        private final Config config;

        public IsActiveProcessor(
                ReconnectableClientSession<CNT> session,
                Config config
        ) {
            this.session = session;
            this.config = config;
        }

        @Override
        public void process() throws Exception {
            if(this.session.isActive()) {
                return;
            }
            logger.info("session is inactive, establish new session......");
            this.session.reconnect(this.config);
        }
    }

    @Override
    protected void closeDone() throws Exception {
        ScheduledTask retryTask = (ScheduledTask) this.taskPool.getTask(RETRY_TASK_NAME);
        retryTask.deactivate();
    }

    @Override
    public CNT reconnect(Config config) throws Exception {
        return this.session.connect(config);
    }
}