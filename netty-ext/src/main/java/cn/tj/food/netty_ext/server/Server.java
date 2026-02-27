package cn.tj.food.netty_ext.server;

import cn.tj.food.netty_ext.handler.Heartbeater;
import cn.tj.food.netty_ext.handler.IdleHandler;
import cn.tj.food.netty_ext.handler.StatusHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Config config;

    public Server(Config config) {
        this.config = config;
    }

    private Channel channel;
    private ChannelInitializer<? extends SocketChannel> initializer;

    public void setInitializer(ChannelInitializer<? extends SocketChannel> initializer) {
        this.initializer = initializer;
    }

    public void startup() throws Exception {
        logger.info("start up server...... [port:{}]", this.config.getPort());
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            ChannelInitializer<? extends SocketChannel> initializer = null;
            if(this.initializer == null) {
                initializer = this.defaultInitializer();
            } else {
                initializer = this.initializer;
            }
            this.channel = new ServerBootstrap()
                    .group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initializer)
                    .bind(this.config.getPort())
                    .sync().channel();
            logger.info("started");
            this.channel.closeFuture().sync();
            logger.info("stopped");
        } finally {
            parentGroup.shutdownGracefully().sync();
            childGroup.shutdownGracefully().sync();
        }
    }

    private ChannelInitializer<? extends SocketChannel> defaultInitializer() {
        StatusHandler statusHandler = new StatusHandler();
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(
                                new IdleHandler(
                                    config.getReadTimeout(),
                                    config.getWriteTimeout(),
                                    config.getReadWriteTimeout(),
                                    TimeUnit.MILLISECONDS
                                )
                        )
                        .addLast(statusHandler)
                        .addLast(new Heartbeater(100L))
                        .addLast(new LineBasedFrameDecoder(512));
            }
        };
    }

    public void shutdown() throws Exception {
        logger.info("shut down server...... [port:{}]", this.config.getPort());
        if(this.channel == null) {
            logger.info("stopped(never started)");
            return;
        }
        this.channel.close();
        this.channel = null;
    }
}