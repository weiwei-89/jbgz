package cn.tj.food.common.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Connector<USER, CNT> {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private final TcpClient<CNT> client;
    protected final Map<String, TcpSession> sessions = new ConcurrentHashMap<>();

    public Connector(TcpClient<CNT> client) {
        this.client = client;
    }

    protected abstract ClientSession<CNT> buildSession(TcpClient<CNT> client) throws Exception;

    protected abstract String generateId(Config config, USER user);

    public void connect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("establish new session...... [session_id:{}](1st)", sessionId);
        ClientSession<CNT> session = this.buildSession(this.client);
        session.connect(config);
        sessions.putIfAbsent(sessionId, session);
    }

    public SessionFuture login(Config config, USER user) throws Exception {
        return this.send(config, user, "login!!!");
    }

    public SessionFuture send(Config config, USER user, String info) throws Exception {
        return this.sessions.get(this.generateId(config, user)).send(info);
    }

    public void disconnect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("disconnect session...... [session_id:{}]", sessionId);
        TcpSession session = this.sessions.get(sessionId);
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
        for(TcpSession session : this.sessions.values()) {
            try {
                session.close();
            } catch(Exception e) {
                logger.error("close error", e);
            }
        }
        this.client.shutdown();
        logger.info("closed");
    }
}