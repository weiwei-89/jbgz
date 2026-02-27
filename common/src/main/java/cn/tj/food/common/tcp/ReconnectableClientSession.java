package cn.tj.food.common.tcp;

public interface ReconnectableClientSession<CNT> extends ClientSession<CNT> {
    CNT reconnect(Config config) throws Exception;
}