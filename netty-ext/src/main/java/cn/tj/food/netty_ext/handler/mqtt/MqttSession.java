package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.TcpClient;
import cn.tj.food.netty_ext.client.Session;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttSession extends Session {
    public MqttSession(TcpClient<Channel> client) {
        super(client);
    }

    // TODO 使用事件传播机制
    public void publish(String topic, String message) {
        MqttHandler mqttHandler = this.connection.pipeline().get(MqttHandler.class);
        mqttHandler.publish(topic, message);
    }

    public void subscribe(String topic, MqttQoS qos) {
        MqttHandler mqttHandler = this.connection.pipeline().get(MqttHandler.class);
        mqttHandler.subscribe(topic, qos);
    }
}