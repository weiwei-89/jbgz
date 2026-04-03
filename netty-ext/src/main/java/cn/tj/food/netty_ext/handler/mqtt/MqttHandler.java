package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.netty_ext.handler.Heartbeater;
import cn.tj.food.netty_ext.util.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class MqttHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    private MqttEventListener eventListener;

    public void addEventListener(MqttEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("MqttHandler added");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof MqttLoginEvent) {
            MqttLoginEvent event = (MqttLoginEvent) evt;
            this.login(ctx, event.getClientId(), event.getUserName(), event.getPassword());
        } else if(evt instanceof MqttPublishEvent) {
            MqttPublishEvent event = (MqttPublishEvent) evt;
            this.publish(ctx, event.getTopic(), event.getMessage());
        } else if(evt instanceof MqttSubscribeEvent) {
            MqttSubscribeEvent event = (MqttSubscribeEvent) evt;
            this.subscribe(ctx, event.getTopic(), event.getQos());
        } else if(evt instanceof Heartbeater.HeartbeatEvent) {
            this.ping(ctx);
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
            case PUBACK:
                logger.info("publish succeed");
                break;
            case PUBLISH:
                MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                String topic = publishMessage.variableHeader().topicName();
                String message = new String(ByteBufUtil.getReadableBytes(publishMessage.payload()));
                if(this.eventListener != null) {
                    this.eventListener.publish(topic, message);
                }
                break;
            case PINGRESP:
                logger.debug("ping succeed");
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

    private void login(ChannelHandlerContext ctx, String clientId, String userName, String password) {
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
                clientId,
                null,
                "".getBytes(StandardCharsets.UTF_8),
                userName,
                password.getBytes(StandardCharsets.UTF_8)
        );
        ctx.writeAndFlush(new MqttConnectMessage(fixedHeader, variableHeader, payload));
    }

    private void publish(ChannelHandlerContext ctx, String topic, String message) {
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
                ctx.alloc().buffer().writeBytes(message.getBytes(StandardCharsets.UTF_8))
        );
        ctx.writeAndFlush(publishMessage);
    }

    private void subscribe(ChannelHandlerContext ctx, String topic, MqttQoS qos) {
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
        ctx.writeAndFlush(subscribeMessage);
    }

    private void ping(ChannelHandlerContext ctx) {
        logger.debug("ping server");
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