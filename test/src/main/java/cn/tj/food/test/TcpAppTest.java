package cn.tj.food.test;

import cn.tj.food.common.FileReader;
import cn.tj.food.common.FileWriter;
import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import cn.tj.food.common.tcp.*;
import cn.tj.food.framework.Initializer;
import cn.tj.food.netty_ext.handler.mqtt.MqttEventListener;
import cn.tj.food.netty_ext.handler.mqtt.MqttSession;
import cn.tj.food.netty_ext.handler.tcp.TcpSession;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
        Initializer initializer = new Initializer(new String[] {"cn.tj.food"});
        initializer.start();
        TcpSession tcp1Session = TcpDriverList.getTcp1Session();
        MqttSession mqtt1Session = MqttDriverList.getMqtt1Session();
        String topic = "/taian/device/status";
        subscribeFunction.apply(mqtt1Session, topic);
        mqtt1Session.addAfterLoginListener(new EventListener() {
            @Override
            public void process() throws Exception {
                subscribeFunction.apply(mqtt1Session, topic);
            }
        });
        mqtt1Session.addEventListener(new MqttEventListener() {
            @Override
            protected void publish(String topic, String message) throws Exception {
                logger.info("mqtt message: {} [topic:{}]", message, topic);
            }
        });
        FileWriter.write("running".getBytes(), ROOT_PATH, RUN_FILE_NAME);
        logger.info("tcp app started");
        taskPool.addScheduledTask(
                "tcp[send-hello]",
                new SendHelloToTcpProcessor(tcp1Session, "hello world!!!"),
                5*1000
        );
        taskPool.addScheduledTask(
                "mqtt[send-hello]",
                new SendHelloToMqttProcessor(mqtt1Session, "/taian/device/control", "hello world!!!"),
                5*1000
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
//        connector.disconnect(config, user);
//        connector.close();
        logger.info("stopped");
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

    private static class SendHelloToTcpProcessor extends SimpleProcessor {
        private final TcpSession tcpSession;
        private final String message;

        public SendHelloToTcpProcessor(
                TcpSession tcpSession,
                String message
        ) {
            this.tcpSession = tcpSession;
            this.message = message;
        }

        @Override
        public void process() throws Exception {
            this.tcpSession.sendMessage(this.message);
        }
    }

    private static class SendHelloToMqttProcessor extends SimpleProcessor {
        private final MqttSession mqttSession;
        private final String topic;
        private final String message;

        public SendHelloToMqttProcessor(
                MqttSession mqttSession,
                String topic,
                String message
        ) {
            this.mqttSession = mqttSession;
            this.topic = topic;
            this.message = message;
        }

        @Override
        public void process() throws Exception {
            this.mqttSession.publish(this.topic, this.message);
        }
    }

    @FunctionalInterface
    private interface SubscribeFunction {
        void apply(MqttSession mqttSession, String topic) throws Exception;
    }

    private static final SubscribeFunction subscribeFunction = (session, topic) -> {
        taskPool.addGeneralTask(
                "subscribe",
                new SubscribeProcessor(session, topic)
        );
    };

    private static class SubscribeProcessor extends SimpleProcessor {
        private final MqttSession session;
        private final String topic;

        public SubscribeProcessor(MqttSession session, String topic) {
            this.session = session;
            this.topic = topic;
        }

        @Override
        public void process() throws Exception {
            this.session.subscribe(this.topic, MqttQoS.AT_MOST_ONCE);
        }
    }
}