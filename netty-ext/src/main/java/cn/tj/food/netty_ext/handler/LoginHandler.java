package cn.tj.food.netty_ext.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class LoginHandler extends MessageToMessageDecoder<String> {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("LoginHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        logger.info("login_info: {}", msg);
        if(this.login()) {

        } else {
            logger.info("login failed");
            ChannelFuture future = ctx.writeAndFlush(
                    ctx.channel()
                            .alloc()
                            .buffer()
                            .writeBytes("login failed".getBytes(StandardCharsets.UTF_8))
            );
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    ctx.channel().close();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("LoginHandler error", cause);
    }

    private boolean login() {
        return false;
    }
}