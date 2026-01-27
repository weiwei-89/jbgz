package cn.tj.food.netty_ext.client;

import cn.tj.food.common.task.ScheduledTaskPool;
import cn.tj.food.common.tcp.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

public class Session extends ClientCommonSession<Channel> {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final TcpClient<Channel> client;

    private Session(TcpClient<Channel> client) {
        this.client = client;
    }

    private Session(
            ScheduledExecutorService executor,
            TcpClient<Channel> client
    ) {
        this.client = client;
    }

    @Override
    public Channel connect(Config config) throws Exception {
        if(this.client == null) {
            return null;
        }
        return this.client.connect(config);
    }

    @Override
    public boolean isActive() {
        if(this.getConnection() == null) {
            return false;
        }
        if(this.getConnection().isWritable()) {
            return true;
        }
        return false;
    }

    @Override
    public SessionFuture send(String info) throws Exception {
        logger.info("sending info......");
        CompleteFuture completeFuture = new CompleteFuture();
        ChannelFuture future = this.getConnection()
                .writeAndFlush(this.getConnection().alloc().buffer().writeBytes(info.getBytes()));
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
        logger.info("close session");
        if(this.getConnection() == null) {
            return;
        }
        this.getConnection().close();
    }

    public static Session create(TcpClient<Channel> client) {
        return new Session(client);
    }

    public static Session create(
            ScheduledExecutorService executor,
            TcpClient<Channel> client
    ) {
        return new Session(executor, client);
    }
}