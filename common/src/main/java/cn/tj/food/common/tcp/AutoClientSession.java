package cn.tj.food.common.tcp;

import cn.tj.food.common.task.Processor;
import cn.tj.food.common.task.ScheduledTask;
import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AutoClientSession<CNT, S extends ClientSession<CNT>> extends ClientSessionAdapter<CNT, S> implements ReconnectableClientSession<CNT> {
    private final TaskPool taskPool = TaskPool.getInstance();
    private final String RETRY_TASK_NAME = "session-retry-task";
    private final long interval;

    public AutoClientSession(S session, long interval) {
        super(session);
        this.interval = interval;
    }

    @Override
    protected void connectDone(Config config) throws Exception {
        this.addListener(config);
    }

    private void addListener(Config config) throws Exception {
        this.taskPool.addScheduledTask(
                RETRY_TASK_NAME,
                new TcpScheduledTask(
                        new IsActiveProcessor<>(this, config),
                        this.interval,
                        this
                )
        );
    }

    private class TcpScheduledTask extends ScheduledTask {
        private final ClientSessionAdapter<CNT, S> session;

        public TcpScheduledTask(
                Processor processor,
                long interval,
                ClientSessionAdapter<CNT, S> session
        ) {
            super(processor, TaskPool.getInstance().getPool(), interval);
            this.session = session;
        }

        @Override
        protected void error(Throwable cause) {
            super.error(cause);
            try {
                this.session.getSession().close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void beforeNext(boolean result) throws Exception {

        }
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
        try {
            CNT connection = this.getSession().connect(config);
            this.reconnected();
            return connection;
        } finally {
            this.reconnectDone(config);
        }
    }

    protected void reconnected() throws Exception {

    }

    protected abstract void reconnectDone(Config config) throws Exception;
}