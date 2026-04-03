package cn.tj.food.netty_ext.handler.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpHandler extends SimpleChannelInboundHandler<TcpMessage> {
    private static final Logger logger = LoggerFactory.getLogger(TcpHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("TcpHandler added");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof TcpLoginEvent) {
            TcpLoginEvent event = (TcpLoginEvent) evt;
            this.login(ctx, event.getClientId(), event.getUserName(), event.getPassword());
        } else if(evt instanceof TcpSendEvent) {
            TcpSendEvent event = (TcpSendEvent) evt;
            this.sendMessage(ctx, event.getMessage());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpMessage msg) throws Exception {
        logger.info("info: {}", msg.getInfo());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("TcpHandler error", cause);
    }

    private void login(ChannelHandlerContext ctx, String clientId, String userName, String password) {
        ctx.writeAndFlush(new LoginInfo(clientId, userName, password));
    }

    private void sendMessage(ChannelHandlerContext ctx, String message) {
        ctx.writeAndFlush(new TcpMessage(message));
    }
}