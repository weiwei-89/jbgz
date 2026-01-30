package cn.tj.food.common.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Connector<USER, CNT> {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private final TcpClient<CNT> client;
    private final Map<String, TcpSession> sessions = new ConcurrentHashMap<>();

    public Connector(TcpClient<CNT> client) {
        this.client = client;
    }

    protected abstract ClientCommonSession<CNT> buildSession(TcpClient<CNT> client) throws Exception;

    protected abstract String generateId(Config config, USER user);

    public void connect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("establish one session[session_id:{}](1st)......", sessionId);
        ClientCommonSession<CNT> session = this.buildSession(this.client);
        session.init(config);
        sessions.putIfAbsent(sessionId, session);
    }

    public SessionFuture send(Config config, USER user, String info) throws Exception {
        return this.sessions.get(this.generateId(config, user)).send(info);
    }

    public void disconnect(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        logger.info("disconnect session[session_id:{}]......", sessionId);
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
                logger.error("close session error", e);
            }
        }
        this.client.shutdown();
        logger.info("closed");
    }
}