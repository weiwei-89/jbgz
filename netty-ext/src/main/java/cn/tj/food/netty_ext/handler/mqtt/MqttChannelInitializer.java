package cn.tj.food.netty_ext.handler.mqtt;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final String clientId;
    private final String userName;
    private final String password;

    public MqttChannelInitializer(
            String clientId,
            String userName,
            String password
    ) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(MqttEncoder.INSTANCE)
                .addLast(new MqttDecoder())
                .addLast(new MqttHandler());
    }
}