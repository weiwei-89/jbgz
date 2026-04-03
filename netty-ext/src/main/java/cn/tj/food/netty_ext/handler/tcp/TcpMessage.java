package cn.tj.food.netty_ext.handler.tcp;

public class TcpMessage {
    private final String info;

    public TcpMessage(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}