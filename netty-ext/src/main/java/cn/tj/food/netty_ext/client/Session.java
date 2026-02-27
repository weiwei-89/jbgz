package cn.tj.food.netty_ext.client;

import cn.tj.food.common.tcp.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class Session implements ClientSession<Channel> {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final TcpClient<Channel> client;

    private Session(TcpClient<Channel> client) {
        this.client = client;
    }

    private Channel connection;

    @Override
    public Channel connect(Config config) throws Exception {
        if(this.client == null) {
            throw new Exception("client is null");
        }
        this.connection = this.client.connect(config.getHost(), config.getPort());
        return this.connection;
    }

    @Override
    public boolean isActive() {
        if(this.connection == null) {
            return false;
        }
        if(this.connection.isWritable()) {
            return true;
        }
        return false;
    }

    @Override
    public SessionFuture send(String info) throws Exception {
        logger.info("send info......");
        CompleteFuture completeFuture = new CompleteFuture();
        ChannelFuture future = this.connection.writeAndFlush(
                this.connection
                        .alloc()
                        .buffer()
                        .writeBytes(info.getBytes(StandardCharsets.UTF_8))
        );
        future.addListener(
                new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()) {
                            completeFuture.complete();
                        } else {
                            completeFuture.error(future.cause());
                        }
                    }
                }
        );
        return completeFuture;
    }

    @Override
    public void read(String info) throws Exception {

    }

    @Override
    public void close() throws Exception {
        logger.info("close session......");
        if(this.connection == null) {
            logger.info("closed(never connected)");
            return;
        }
        this.connection.close();
        logger.info("closed");
    }

    public static Session create(TcpClient<Channel> client) throws Exception {
        return new Session(client);
    }
}