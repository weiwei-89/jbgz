package cn.tj.food.common.tcp;

public interface TcpClient<C> {
    C connect(Config config) throws Exception;

    void shutdown();
}