package cn.tj.food.netty_ext.handler.tcp;

import cn.tj.food.common.tcp.EventListener;
import cn.tj.food.common.tcp.TcpClient;
import cn.tj.food.netty_ext.client.Session;
import io.netty.channel.Channel;

public class TcpSession extends Session {
    public TcpSession(TcpClient<Channel> client) {
        super(client);
    }

    public void login(String clientId, String userName, String password) {
        this.connection.pipeline().fireUserEventTriggered(new TcpLoginEvent(clientId, userName, password));
    }

    public void sendMessage(String message) {
        this.connection.pipeline().fireUserEventTriggered(new TcpSendEvent(message));
    }

    private EventListener reloginListener;

    public void addReloginListener(EventListener listener) {
        this.reloginListener = listener;
    }

    public void relogin() throws Exception {
        if(this.reloginListener == null) {
            return;
        }
        this.reloginListener.process();
    }
}