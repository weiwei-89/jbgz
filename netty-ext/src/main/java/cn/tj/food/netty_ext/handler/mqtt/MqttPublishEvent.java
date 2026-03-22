package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.Event;

public class MqttPublishEvent implements Event {
    private final String topic;
    private final String message;

    public MqttPublishEvent(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    @Override
    public Type type() {
        return Type.MQTT_PUBLISH;
    }

    public String getTopic() {
        return topic;
    }
    public String getMessage() {
        return message;
    }
}