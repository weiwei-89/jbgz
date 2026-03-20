package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.handler.IdleHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

import java.util.concurrent.TimeUnit;

public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final long READ_TIMEOUT = 0L;
    private static final long WRITE_TIMEOUT = 10*1000;
    private static final long READ_WRITE_TIMEOUT = 0L;

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
                .addLast(
                        new IdleHandler(
                                READ_TIMEOUT,
                                WRITE_TIMEOUT,
                                READ_WRITE_TIMEOUT,
                                TimeUnit.MILLISECONDS
                        )
                )
                .addLast(new MqttHandler(this.clientId, this.userName, this.password));
    }
}