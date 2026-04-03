package cn.tj.food.netty_ext.handler.tcp;

import cn.tj.food.netty_ext.Event;

public class TcpSendEvent implements Event {
    private final String message;

    public TcpSendEvent(String message) {
        this.message = message;
    }

    @Override
    public Type type() {
        return Type.TCP_SEND;
    }

    public String getMessage() {
        return message;
    }
}