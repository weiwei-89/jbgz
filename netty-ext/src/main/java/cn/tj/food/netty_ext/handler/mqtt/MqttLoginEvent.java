package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.Event;

public class MqttLoginEvent implements Event {
    private final String clientId;
    private final String userName;
    private final String password;

    public MqttLoginEvent(
            String clientId,
            String userName,
            String password
    ) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public Type type() {
        return Type.MQTT_LOGIN;
    }

    public String getClientId() {
        return clientId;
    }
    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
}