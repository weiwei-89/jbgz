package cn.tj.food.common.tcp;

public interface ClientSession<CNT> extends TcpSession {
    CNT connect(Config config) throws Exception;
}