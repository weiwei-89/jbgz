package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("MqttHandler added");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        switch(msg.fixedHeader().messageType()) {
            case CONNACK:
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) msg;
                if(connAckMessage.variableHeader().connectReturnCode() != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                    logger.info("connect failed");
                    ctx.close();
                }
                logger.info("connect succeed");
                break;
            case SUBACK:
                logger.info("subscribe succeed");
                break;
            case PUBLISH:
                MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                ByteBuf payload = publishMessage.payload();
                logger.info("topic:{}, info:{}",
                        publishMessage.variableHeader().topicName(),
                        new String(ByteBufUtil.getReadableBytes(payload)));
                break;
            default:
                throw new Exception(String.format("unhandled message type: %s", msg.fixedHeader().messageType()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("MqttHandler error", cause);
    }
}