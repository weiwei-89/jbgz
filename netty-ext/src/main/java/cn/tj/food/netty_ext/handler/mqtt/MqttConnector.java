package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.*;
import io.netty.channel.Channel;

public abstract class MqttConnector extends Connector<User, Channel, AutoClientSession<Channel, MqttSession>> {
    public MqttConnector(TcpClient<Channel> client) {
        super(client);
    }

    @Override
    protected AutoClientSession<Channel, MqttSession> buildSession(TcpClient<Channel> client) throws Exception {
        return new AutoClientSession<Channel, MqttSession>(
                new MqttSession(client),
                10*1000
        ) {
            @Override
            protected void reconnectDone(Config config) throws Exception {
                MqttSession session = this.getSession();
                session.relogin();
                session.afterLogin();
            }
        };
    }

    public MqttSession getMqttSession(Config config, User user) {
        return this.getSession(config, user).getSession();
    }
}