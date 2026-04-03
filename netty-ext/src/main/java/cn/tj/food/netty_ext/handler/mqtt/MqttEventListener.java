package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.EventListener;

public abstract class MqttEventListener implements EventListener {
    @Override
    public void process() throws Exception {

    }

    abstract protected void publish(String topic, String message) throws Exception;
}