package cn.tj.food.netty_ext.handler.tcp;

import cn.tj.food.common.tcp.*;
import io.netty.channel.Channel;

public abstract class TcpConnector extends Connector<User, Channel, AutoClientSession<Channel, TcpSession>> {
    public TcpConnector(TcpClient<Channel> client) {
        super(client);
    }

    @Override
    protected AutoClientSession<Channel, TcpSession> buildSession(TcpClient<Channel> client) throws Exception {
        return new AutoClientSession<Channel, TcpSession>(
                new TcpSession(client),
                10*1000
        ) {
            @Override
            protected void reconnectDone(Config config) throws Exception {
                TcpSession session = this.getSession();
                session.relogin();
            }
        };
    }

    public TcpSession getTcpSession(Config config, User user) {
        return this.getSession(config, user).getSession();
    }
}