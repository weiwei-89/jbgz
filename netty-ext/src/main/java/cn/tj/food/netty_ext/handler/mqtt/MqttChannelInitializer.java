package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.handler.Heartbeater;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final long heartbeat;

    public MqttChannelInitializer(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(MqttEncoder.INSTANCE)
                .addLast(new MqttDecoder())
                .addLast(new Heartbeater(this.heartbeat))
                .addLast(new MqttHandler());
    }
}