package cn.tj.food.netty_ext;

public interface Event {
    enum Type {
        TCP_LOGIN, TCP_SEND,
        MQTT_LOGIN, MQTT_PUBLISH, MQTT_SUBSCRIBE
    }

    Type type();
}