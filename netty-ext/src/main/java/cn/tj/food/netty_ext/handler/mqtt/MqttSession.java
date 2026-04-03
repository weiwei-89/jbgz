package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.tcp.EventListener;
import cn.tj.food.common.tcp.TcpClient;
import cn.tj.food.netty_ext.client.Session;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttSession extends Session {
    public MqttSession(TcpClient<Channel> client) {
        super(client);
    }

    public void login(String clientId, String userName, String password) {
        this.connection.pipeline().fireUserEventTriggered(new MqttLoginEvent(clientId, userName, password));
    }

    public void publish(String topic, String message) {
        this.connection.pipeline().fireUserEventTriggered(new MqttPublishEvent(topic, message));
    }

    public void subscribe(String topic, MqttQoS qos) {
        this.connection.pipeline().fireUserEventTriggered(new MqttSubscribeEvent(topic, qos));
    }

    private EventListener reloginListener;

    public void addReloginListener(EventListener listener) {
        this.reloginListener = listener;
    }

    public void relogin() throws Exception {
        if(this.reloginListener == null) {
            return;
        }
        this.reloginListener.process();
    }

    private EventListener afterLoginListener;

    public void addAfterLoginListener(EventListener listener) {
        this.afterLoginListener = listener;
    }

    public void afterLogin() throws Exception {
        if(this.afterLoginListener == null) {
            return;
        }
        this.afterLoginListener.process();
    }

    public void addEventListener(MqttEventListener eventListener) {
        MqttHandler mqttHandler = this.connection.pipeline().get(MqttHandler.class);
        mqttHandler.addEventListener(eventListener);
    }
}