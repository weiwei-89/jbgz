package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.util.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class MqttHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    private final String clientId;
    private final String userName;
    private final String password;

    public MqttHandler(String clientId, String userName, String password) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
    }

    private Channel channel;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("MqttHandler added");
        this.channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNECT,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0
        );
        MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
                "MQTT",
                4,
                true,
                true,
                false,
                MqttQoS.AT_MOST_ONCE.value(),
                false,
                true,
                60
        );
        MqttConnectPayload payload = new MqttConnectPayload(
                this.clientId,
                null,
                "".getBytes(StandardCharsets.UTF_8),
                this.userName,
                this.password.getBytes(StandardCharsets.UTF_8)
        );
        ctx.writeAndFlush(new MqttConnectMessage(fixedHeader, variableHeader, payload));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.WRITER_IDLE) {
                logger.info("send heartbeat");
                MqttFixedHeader header = new MqttFixedHeader(
                        MqttMessageType.PINGREQ,
                        false,
                        MqttQoS.AT_MOST_ONCE,
                        false,
                        0
                );
                ctx.writeAndFlush(new MqttMessage(header));
            }
        }
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
                logger.info("topic:{}, info:{}",
                        publishMessage.variableHeader().topicName(),
                        new String(ByteBufUtil.getReadableBytes(publishMessage.payload())));
                break;
            case PINGRESP:
                logger.info("heartbeat!!!");
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

    public void publish(String topic, String message) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.PUBLISH,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                0
        );
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 12345);
        MqttPublishMessage publishMessage = new MqttPublishMessage(
                fixedHeader,
                variableHeader,
                this.channel
                        .alloc()
                        .buffer()
                        .writeBytes(message.getBytes(StandardCharsets.UTF_8))
        );
        this.channel.writeAndFlush(publishMessage);
    }

    public void subscribe(String topic, MqttQoS qos) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.SUBSCRIBE,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                0
        );
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(54321);
        MqttSubscribePayload payload = new MqttSubscribePayload(
                Collections.singletonList(new MqttTopicSubscription(topic, qos))
        );
        MqttSubscribeMessage subscribeMessage = new MqttSubscribeMessage(fixedHeader, variableHeader, payload);
        this.channel.writeAndFlush(subscribeMessage);
    }
}