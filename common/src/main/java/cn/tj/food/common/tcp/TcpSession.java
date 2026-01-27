package cn.tj.food.common.tcp;

public interface TcpSession {
    boolean isActive();

    SessionFuture send(String info) throws Exception;

    void read(String info) throws Exception;

    void close() throws Exception;
}