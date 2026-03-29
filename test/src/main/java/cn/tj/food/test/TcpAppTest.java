package cn.tj.food.test;

import cn.tj.food.common.FileReader;
import cn.tj.food.common.FileWriter;
import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import cn.tj.food.common.tcp.*;
import cn.tj.food.framework.ConfReader;
import cn.tj.food.framework.DriverInitializer;
import cn.tj.food.netty_ext.client.Client;
import cn.tj.food.netty_ext.client.Session;
import cn.tj.food.netty_ext.handler.mqtt.MqttDriver;
import cn.tj.food.netty_ext.handler.mqtt.MqttSession;
import io.netty.channel.Channel;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TcpAppTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpAppTest.class);
    private static final String SERVER_HOST = "server.host";
    private static final String SERVER_PORT = "server.port";
    private static final Object client = new Object();
    private static volatile boolean running = true;
    private static final TaskPool taskPool = TaskPool.getInstance();
    private static final String ROOT_PATH = "D:\\edward\\test\\jbgz\\test";
    private static final String RUN_FILE_NAME = "tcp-app.run";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(SERVER_HOST).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(SERVER_PORT).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String serverHost = cmd.getOptionValue(SERVER_HOST);
        int serverPort = Integer.parseInt(cmd.getOptionValue(SERVER_PORT));
        Config config = new Config();
        config.setHost(serverHost);
        config.setPort(serverPort);
        User user = new User();
        user.setName("edward");
        user.setPassword("123456");
        ConfReader confReader = new ConfReader();
        confReader.readFromRoot("jbgz.conf");
        List<ConfReader.Config> configList = confReader.getConfigList();
        Map<String, String> mqttConfigMap = configList.stream()
                .filter(c -> c.getKey().startsWith("driver.mqtt"))
                .collect(
                        Collectors.toMap(
                                c -> c.getKey().substring("driver.mqtt.".length()),
                                ConfReader.Config::getValue,
                                (c1, c2) -> c1
                        )
                );
        DriverInitializer driverInitializer = new DriverInitializer("cn.tj.food.netty_ext.handler");
        driverInitializer.load();
        MqttDriver mqttDriver = driverInitializer.getInstance("mqtt3.1.1", MqttDriver.class);
        MqttSession mqttSession = mqttDriver.connect(mqttConfigMap);
        Connector<User, Channel, AutoClientSession<Channel, Session>> connector = new Connector<User, Channel, AutoClientSession<Channel, Session>>(Client.build()) {
            @Override
            protected AutoClientSession<Channel, Session> buildSession(TcpClient<Channel> client) throws Exception {
                return new AutoClientSession<Channel, Session>(
                        Session.create(client),
                        10*1000
                ) {
                    @Override
                    protected void reconnectDone(Config config) throws Exception {

                    }
                };
            }

            @Override
            protected String generateId(Config config, User user) {
                return String.format("%s:%d-%s", config.getHost(), config.getPort(), user.getName());
            }
        };
        FileWriter.write("running".getBytes(), ROOT_PATH, RUN_FILE_NAME);
        logger.info("tcp app started");
        taskPool.addGeneralTask(
                "connect",
                new ConnectProcessor(connector, config, user)
        );
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    logger.info("shutdown tcp app...... (console)");
                                    synchronized(TcpAppTest.client) {
                                        TcpAppTest.running = false;
                                        TcpAppTest.client.notifyAll();
                                    }
                                }
                        )
                );
        taskPool.addScheduledTask(
                "shutdown",
                new ShutdownProcessor(),
                5*1000
        );
        synchronized(TcpAppTest.client) {
            while(TcpAppTest.running) {
                TcpAppTest.client.wait();
            }
        }
        connector.disconnect(config, user);
        connector.close();
        logger.info("stopped");
    }

    private static class ConnectProcessor extends SimpleProcessor {
        private final Connector<User, Channel, AutoClientSession<Channel, Session>> connector;
        private final Config config;
        private final User user;

        public ConnectProcessor(
                Connector<User, Channel, AutoClientSession<Channel, Session>> connector,
                Config config,
                User user
        ) {
            this.connector = connector;
            this.config = config;
            this.user = user;
        }

        @Override
        public void process() throws Exception {
            this.connector.connect(this.config, this.user);
        }
    }

    private static class ShutdownProcessor extends SimpleProcessor {
        private static final Logger logger = LoggerFactory.getLogger(ShutdownProcessor.class);

        @Override
        public void process() throws Exception {
            logger.debug("[status]listening......");
            FileReader fileReader = new FileReader();
            String runInfo = fileReader.read(TcpAppTest.ROOT_PATH+File.separator+TcpAppTest.RUN_FILE_NAME);
            if(!"shutdown".equals(runInfo)) {
                logger.debug("[status]running......");
                return;
            }
            logger.info("[status]shutdown tcp app...... (.run文件)");
            synchronized(TcpAppTest.client) {
                TcpAppTest.running = false;
                TcpAppTest.client.notifyAll();
            }
        }
    }
}