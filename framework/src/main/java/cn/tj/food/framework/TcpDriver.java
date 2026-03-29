package cn.tj.food.framework;

import cn.tj.food.common.tcp.ClientSession;

import java.util.Map;

public interface TcpDriver {
    void init() throws Exception;

    ClientSession<?> connect(Map<String, String> param) throws Exception;
}