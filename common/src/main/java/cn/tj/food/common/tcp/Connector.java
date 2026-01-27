package cn.tj.food.common.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Connector<USER, CNT> {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private final TcpClient<CNT> client;
    private final ClientSessionManager<USER, CNT, ClientCommonSession<CNT>> sessionManager;

    public Connector(
            TcpClient<CNT> client,
            ClientSessionManager<USER, CNT, ClientCommonSession<CNT>> sessionManager
    ) {
        this.client = client;
        this.sessionManager = sessionManager;
    }

    protected abstract ClientCommonSession<CNT> buildSession(TcpClient<CNT> client) throws Exception;

    public void connect(Config config, USER user) throws Exception {
        ClientCommonSession<CNT> session = this.buildSession(this.client);
        try {
            logger.info("current session is inactive, trying to establish(1st)......");
            session.init(config);
            this.sessionManager.addSession(config, user, session);
        } catch(Exception e) {
            logger.error("session establishment(1st) failed", e);
        }
    }

    public SessionFuture send(Config config, USER user, String info) throws Exception {
        return this.sessionManager.getSession(config, user).send(info);
    }

    public void close(Config config, USER user) throws Exception {
        this.sessionManager.closeSession(config, user);
    }

    // TODO 清理sessionManager
    public void shutdown() throws Exception {
        logger.info("shutting down connector......");
        if(this.client == null) {
            logger.info("done(not started)");
            return;
        }
        this.client.shutdown();
        logger.info("done");
    }
}