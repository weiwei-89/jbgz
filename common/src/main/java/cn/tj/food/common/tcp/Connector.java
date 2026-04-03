package cn.tj.food.common.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Connector<USER, CNT, S extends ClientSession<CNT>> {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private final TcpClient<CNT> client;
    private final Map<String, S> sessions = new ConcurrentHashMap<>();

    public Connector(TcpClient<CNT> client) {
        this.client = client;
    }

    protected abstract S buildSession(TcpClient<CNT> client) throws Exception;

    protected abstract String generateId(Config config, USER user);

    public S connect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("establish new session...... [session_id:{}](1st)", sessionId);
        S session = this.buildSession(this.client);
        try {
            session.connect(config);
        } catch(Exception e) {
            logger.warn(String.format("connect failed [session_id:%s]", sessionId), e);
        } finally {
            this.sessions.putIfAbsent(sessionId, session);
        }
        return session;
    }

    public void disconnect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("disconnect session...... [session_id:{}]", sessionId);
        S session = this.sessions.get(sessionId);
        if(session == null) {
            return;
        }
        try {
            session.close();
        } finally {
            this.sessions.remove(sessionId);
        }
    }

    public void close() throws Exception {
        logger.info("close connector......");
        if(this.client == null) {
            logger.info("closed(never started)");
            return;
        }
        for(S session : this.sessions.values()) {
            try {
                session.close();
            } catch(Exception e) {
                logger.error("close error", e);
            }
        }
        this.sessions.clear();
        this.client.shutdown();
        logger.info("closed");
    }

    public S getSession(Config config, USER user) {
        return this.sessions.get(this.generateId(config, user));
    }
}