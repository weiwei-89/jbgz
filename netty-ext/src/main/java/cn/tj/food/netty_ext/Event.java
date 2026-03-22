package cn.tj.food.netty_ext;

public interface Event {
    enum Type {
        MQTT_LOGIN, MQTT_PUBLISH, MQTT_SUBSCRIBE
    }

    Type type();
}