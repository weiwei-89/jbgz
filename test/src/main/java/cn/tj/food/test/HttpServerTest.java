package cn.tj.food.test;

import cn.tj.food.common.FileReader;
import cn.tj.food.common.FileWriter;
import cn.tj.food.common.router.ApiLoader;
import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import cn.tj.food.netty_ext.handler.HttpDispatchHandler;
import cn.tj.food.netty_ext.handler.HttpResponseHandler;
import cn.tj.food.netty_ext.handler.StatusHandler;
import cn.tj.food.netty_ext.server.Config;
import cn.tj.food.netty_ext.server.Server;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HttpServerTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerTest.class);
    private static final String HTTP_PORT = "http.port";
    private static final Object server = new Object();
    private static volatile boolean running = true;
    private static final TaskPool taskPool = TaskPool.getInstance();
    private static final String ROOT_PATH = "D:\\edward\\test\\jbgz\\test";
    private static final String RUN_FILE_NAME = "http-server.run";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(HTTP_PORT).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int httpPort = Integer.parseInt(cmd.getOptionValue(HTTP_PORT));
        Config config = new Config();
        config.setPort(httpPort);
        StatusHandler statusHandler = new StatusHandler();
        ApiLoader apiLoader = new ApiLoader("cn.tj.food.test.controller");
        apiLoader.init();
        Server server = new Server(config);
        server.setInitializer(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(statusHandler)
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024*1024))
                                .addLast("HttpDispatchHandler", new HttpDispatchHandler(apiLoader))
                                .addLast("HttpResponseHandler", new HttpResponseHandler());
                    }
                }
        );
        FileWriter.write("running".getBytes(), ROOT_PATH, RUN_FILE_NAME);
        logger.info("http server started");
        taskPool.addGeneralTask(
                "startup",
                new StartupProcessor(server)
        );
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    logger.info("shutdown http server...... (console)");
                                    synchronized(HttpServerTest.server) {
                                        HttpServerTest.running = false;
                                        HttpServerTest.server.notifyAll();
                                    }
                                }
                        )
                );
        taskPool.addScheduledTask(
                "shutdown",
                new ShutdownProcessor(),
                5*1000
        );
        synchronized(HttpServerTest.server) {
            while(HttpServerTest.running) {
                HttpServerTest.server.wait();
            }
        }
        server.shutdown();
        logger.info("stopped");
    }

    private static class StartupProcessor extends SimpleProcessor {
        private final Server server;

        public StartupProcessor(Server server) {
            this.server = server;
        }

        @Override
        public void process() throws Exception {
            this.server.startup();
        }
    }

    private static class ShutdownProcessor extends SimpleProcessor {
        private static final Logger logger = LoggerFactory.getLogger(ShutdownProcessor.class);

        @Override
        public void process() throws Exception {
            logger.debug("[status]listening......");
            FileReader fileReader = new FileReader();
            String runInfo = fileReader.read(HttpServerTest.ROOT_PATH+ File.separator+HttpServerTest.RUN_FILE_NAME);
            if(!"shutdown".equals(runInfo)) {
                logger.debug("[status]running......");
                return;
            }
            logger.info("[status]shutdown http server...... (.run文件)");
            synchronized(HttpServerTest.server) {
                HttpServerTest.running = false;
                HttpServerTest.server.notifyAll();
            }
        }
    }
}