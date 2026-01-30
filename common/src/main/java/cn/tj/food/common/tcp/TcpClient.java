package cn.tj.food.common.tcp;

public interface TcpClient<CNT> {
    CNT connect(String host, int port) throws Exception;

    void shutdown() throws Exception;
}