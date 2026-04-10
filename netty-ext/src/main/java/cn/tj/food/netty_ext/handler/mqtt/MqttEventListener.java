package cn.tj.food.netty_ext.handler.mqtt;

public abstract class MqttEventListener {
    protected void afterLogin() throws Exception {

    }

    protected void publish(String topic, String message) throws Exception {

    }
}