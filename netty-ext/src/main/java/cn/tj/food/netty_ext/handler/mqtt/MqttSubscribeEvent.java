package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.Event;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttSubscribeEvent implements Event {
    private final String topic;
    private final MqttQoS qos;

    public MqttSubscribeEvent(String topic, MqttQoS qos) {
        this.topic = topic;
        this.qos = qos;
    }

    @Override
    public Type type() {
        return Type.MQTT_SUBSCRIBE;
    }

    public String getTopic() {
        return topic;
    }
    public MqttQoS getQos() {
        return qos;
    }
}