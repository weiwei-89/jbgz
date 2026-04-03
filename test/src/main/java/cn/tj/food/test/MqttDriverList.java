package cn.tj.food.test;

import cn.tj.food.framework.Jbgz;
import cn.tj.food.framework.TcpClient;
import cn.tj.food.netty_ext.handler.mqtt.MqttSession;

@Jbgz(configPrefix="driver.mqtt")
public class MqttDriverList {
    @TcpClient(protocol="mqtt3.1.1",name="mqtt1")
    private static MqttSession mqtt1Session;

    public static MqttSession getMqtt1Session() {
        return mqtt1Session;
    }
}