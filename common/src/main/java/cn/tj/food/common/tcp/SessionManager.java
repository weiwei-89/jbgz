package cn.tj.food.common.tcp;

public interface SessionManager<USER, S extends TcpSession> {
    void addSession(Config config, USER user, S session) throws Exception;

    S getSession(Config config, USER user) throws Exception;

    void closeSession(Config config, USER user) throws Exception;
}