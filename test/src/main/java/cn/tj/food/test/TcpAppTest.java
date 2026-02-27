package cn.tj.food.test;

import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpAppTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpAppTest.class);
    private static final String SERVER_HOST = "server.host";
    private static final String SERVER_PORT = "server.port";
    private static final Object client = new Object();
    private static volatile boolean running = true;
    private static final TaskPool taskPool = TaskPool.getInstance();

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(SERVER_HOST).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(SERVER_PORT).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String serverHost = cmd.getOptionValue(SERVER_HOST);
        int serverPort = Integer.parseInt(cmd.getOptionValue(SERVER_PORT));
        logger.info("tcp app started");
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    logger.info("close tcp app......");
                                    synchronized(client) {
                                        running = false;
                                        client.notifyAll();
                                    }
                                }
                        )
                );
        taskPool.addGeneralTask(
                "tcp-app",
                new TcpClientProcessor()
        );
        synchronized(client) {
            while(running) {
                client.wait();
            }
        }
        logger.info("stopped");
    }

    private static class TcpClientProcessor extends SimpleProcessor {
        @Override
        public void process() throws Exception {

        }
    }
}