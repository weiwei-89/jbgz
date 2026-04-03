package cn.tj.food.netty_ext.handler;

import cn.tj.food.netty_ext.handler.tcp.LoginInfo;
import cn.tj.food.netty_ext.handler.tcp.TcpMessage;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoginHandler extends MessageToMessageDecoder<LoginInfo> {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("LoginHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, LoginInfo msg, List<Object> out) throws Exception {
        logger.info("login_info: {}", JSON.toJSONString(msg));
        if(this.login(msg)) {

        } else {
            logger.info("login failed");
            ChannelFuture future = ctx.writeAndFlush(new TcpMessage("login failed"));
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

    private boolean login(LoginInfo loginInfo) {
        return false;
    }
}