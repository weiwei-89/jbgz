package cn.tj.food.test;

import cn.tj.food.framework.Jbgz;
import cn.tj.food.framework.TcpClient;
import cn.tj.food.netty_ext.handler.tcp.TcpSession;

@Jbgz(configPrefix="driver.tcp")
public class TcpDriverList {
    @TcpClient(protocol="tcp1.0",name="tcp1")
    private static TcpSession tcp1Session;

    public static TcpSession getTcp1Session() {
        return tcp1Session;
    }
}