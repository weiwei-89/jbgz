package cn.tj.food.test;

import cn.tj.food.common.tcp.*;
import cn.tj.food.netty_ext.client.Client;
import cn.tj.food.netty_ext.client.Session;
import io.netty.channel.Channel;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpClientTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpClientTest.class);
    private static final String TARGET_HOST = "target.host";
    private static final String TARGET_PORT = "target.port";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(TARGET_HOST).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(TARGET_PORT).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String targetHost = cmd.getOptionValue(TARGET_HOST);
        int targetPort = Integer.parseInt(cmd.getOptionValue(TARGET_PORT));
        Config config = new Config();
        config.setHost(targetHost);
        config.setPort(targetPort);
        User user = new User();
        user.setName("edward");
        user.setPassword("123456");
        Connector<User, Channel> connector = null;
        try {
            connector = new Connector<User, Channel>(Client.build()) {
                @Override
                protected ClientCommonSession<Channel> buildSession(TcpClient<Channel> client) throws Exception {
                    return Session.create(client);
                }

                @Override
                protected String generateId(Config config, User user) {
                    return String.format("%s:%d-%s", config.getHost(), config.getPort(), user.getName());
                }
            };
            connector.connect(config, user);
            connector.send(config, user, "hello");
            connector.disconnect(config, user);
        } finally {
            if(connector != null) {
                connector.close();
            }
        }
    }
}