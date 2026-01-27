package cn.tj.food.common.tcp;

import java.util.HashMap;
import java.util.Map;

public abstract class CommonSessionManager<USER, S extends TcpSession> implements SessionManager<USER, S> {
    // TODO 加读写锁保证线程安全
    private final Map<String, S> sessions = new HashMap<>();

    @Override
    public void addSession(Config config, USER user, S session) throws Exception {
        this.sessions.put(this.generateId(config, user), session);
    }

    @Override
    public S getSession(Config config, USER user) throws Exception {
        S session = this.sessions.get(this.generateId(config, user));
        if(session == null) {
            throw new Exception("session does not exist");
        }
        return session;
    }

    @Override
    public void closeSession(Config config, USER user) throws Exception {
        String sessionId = this.generateId(config, user);
        S session = this.sessions.get(sessionId);
        if(session == null) {
            return;
        }
        session.close();
        this.sessions.remove(sessionId);
    }

    protected abstract String generateId(Config config, USER user);
}