package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.*;
import io.netty.channel.Channel;

public abstract class MqttConnector extends Connector<User, Channel> {
    public MqttConnector(TcpClient<Channel> client) {
        super(client);
    }

    @Override
    protected ClientSession<Channel> buildSession(TcpClient<Channel> client) throws Exception {
        return new AutoClientSession<Channel>(
                new MqttSession(client),
                10*1000
        ) {
            @Override
            protected void reconnectDone(Config config) throws Exception {

            }
        };
    }

    public void publish(Config config, User user, String topic, String message) {
        ClientSessionAdapter<Channel> sessionAdapter = (ClientSessionAdapter<Channel>) this.sessions.get(this.generateId(config, user));
        MqttSession mqttSession = (MqttSession) sessionAdapter.getSession();
        mqttSession.publish(topic, message);
    }
}