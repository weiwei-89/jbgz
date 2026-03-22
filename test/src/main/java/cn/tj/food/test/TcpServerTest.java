package cn.tj.food.test;

import cn.tj.food.common.DataUtil;
import cn.tj.food.netty_ext.codec.decoder.StringMessageConvertor;
import cn.tj.food.netty_ext.codec.encoder.Appender;
import cn.tj.food.netty_ext.handler.Heartbeater;
import cn.tj.food.netty_ext.handler.IdleHandler;
import cn.tj.food.netty_ext.handler.LoginHandler;
import cn.tj.food.netty_ext.handler.StatusHandler;
import cn.tj.food.netty_ext.server.Config;
import cn.tj.food.netty_ext.server.Server;
import cn.tj.food.netty_ext.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TcpServerTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerTest.class);
    private static final String LISTEN_PORT = "listen.port";
    private static final long READ_TIMEOUT = 300*1000;
    private static final long WRITE_TIMEOUT = 0L;
    private static final long READ_WRITE_TIMEOUT = 0L;

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(LISTEN_PORT).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int listenPort = Integer.parseInt(cmd.getOptionValue(LISTEN_PORT));
        Config config = new Config();
        config.setPort(listenPort);
        StatusHandler statusHandler = new StatusHandler();
        StringMessageConvertor stringMessageConvertor = new StringMessageConvertor();
        Server server = new Server(config);
        server.setInitializer(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(
                                        new IdleHandler(
                                            READ_TIMEOUT,
                                            WRITE_TIMEOUT,
                                            READ_WRITE_TIMEOUT,
                                            TimeUnit.MILLISECONDS
                                        )
                                )
                                .addLast(statusHandler)
                                .addLast(new Appender("\r\n".getBytes()))
                                .addLast(new Heartbeater(100L))
        //                        .addLast(new FrameDecoder(new byte[]{0x3D}, 8))
                                .addLast(new LineBasedFrameDecoder(512))
                                .addLast(stringMessageConvertor)
                                .addLast(new LoginHandler());
//                                .addLast(
//                                        new ChannelInboundHandlerAdapter() {
//                                            @Override
//                                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//                                                super.userEventTriggered(ctx, evt);
//                                                if(evt instanceof Heartbeater.HeartbeatEvent) {
//                                                    logger.info("tick......");
//                                                }
//                                            }
//
//                                            @Override
//                                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                                try {
//                                                    if(msg instanceof ByteBuf) {
//                                                        ByteBuf buffer = (ByteBuf) msg;
//                                                        logger.info("hex: {}",
//                                                                DataUtil.toHexString(ByteBufUtil.getReadableBytes(buffer)));
//                                                    }
//                                                } finally {
//                                                    ReferenceCountUtil.release(msg, ReferenceCountUtil.refCnt(msg));
//                                                }
//                                            }
//                                        }
//                                );
                    }
                }
        );
        server.startup();
    }
}