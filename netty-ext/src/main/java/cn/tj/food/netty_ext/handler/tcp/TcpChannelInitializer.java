package cn.tj.food.netty_ext.handler.tcp;

import cn.tj.food.netty_ext.codec.encoder.Appender;
import cn.tj.food.netty_ext.handler.Heartbeater;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final long heartbeat;

    public TcpChannelInitializer(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new Appender("\r\n".getBytes()))
                .addLast(new LineBasedFrameDecoder(512))
                .addLast(new Heartbeater(this.heartbeat))
                .addLast(new TcpHandler());
    }
}