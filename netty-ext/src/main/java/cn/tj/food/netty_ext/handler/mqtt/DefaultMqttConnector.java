package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.Config;
import cn.tj.food.common.tcp.User;
import cn.tj.food.netty_ext.client.Client;

public class DefaultMqttConnector extends MqttConnector {
    public DefaultMqttConnector(long heartbeat) {
        super(Client.build(new MqttChannelInitializer(heartbeat)));
    }

    @Override
    protected String generateId(Config config, User user) {
        return String.format("%s:%d-%s", config.getHost(), config.getPort(), user.getName());
    }
}