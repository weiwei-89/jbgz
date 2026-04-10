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

    public void login(String clientId, String userName, String password) throws Exception {
        if(!this.isActive()) {
            throw new Exception("login failed [session inactivated]");
        }
        this.connection.pipeline().fireUserEventTriggered(new MqttLoginEvent(clientId, userName, password));
    }

    public void publish(String topic, String message) throws Exception {
        if(!this.isActive()) {
            throw new Exception("publish failed [session inactivated]");
        }
        this.connection.pipeline().fireUserEventTriggered(new MqttPublishEvent(topic, message));
    }

    public void subscribe(String topic, MqttQoS qos) throws Exception {
        if(!this.isActive()) {
            throw new Exception("subscribe failed [session inactivated]");
        }
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

    private MqttEventListener eventListener;

    public void addEventListener(MqttEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public MqttEventListener getMqttEventListener() {
        return this.eventListener;
    }
}