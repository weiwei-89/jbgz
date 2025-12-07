package cn.tj.food.netty_ext.server;

import cn.tj.food.common.tcp.User;
import io.netty.channel.Channel;

public class Session {
    private Channel channel;
    private User user;

    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}