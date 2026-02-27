package cn.tj.food.netty_ext.handler;

import cn.tj.food.common.router.ApiParam;
import cn.tj.food.netty_ext.Connector;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;

public class HttpZeroCopyDownloadHandler extends MessageToMessageDecoder<Connector> {
    private static final Logger logger = LoggerFactory.getLogger(HttpZeroCopyDownloadHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("HttpZeroCopyDownloadHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Connector connector, List<Object> out) throws Exception {
        ApiParam param = connector.getParam();
        String fileId = param.getString("file_id", "");
        File file = new File("D:\\edward\\test\\jbgz\\test\\files\\三部曲影史票房.jpg");
        HttpResponse httpResponse = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK
        );
        if(connector.getDownloadMode() == Connector.DOWNLOAD_MODE.PREVIEW) {
            String mimeType = Files.probeContentType(file.toPath());
            httpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, mimeType)
                    .set(HttpHeaderNames.CONTENT_DISPOSITION, String.format("%s;%s=\"%s\"", "inline", HttpHeaderValues.FILENAME, "haha.jpg"))
                    .set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        } else {
            httpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM)
                    .set(HttpHeaderNames.CONTENT_DISPOSITION, String.format("%s;%s=\"%s\"", HttpHeaderValues.ATTACHMENT, HttpHeaderValues.FILENAME, "haha.jpg"))
                    .set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        }
        ctx.write(httpResponse);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileRegion fr = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
        ChannelFuture future = ctx.write(fr, ctx.newProgressivePromise());
        future.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                logger.info("transferring...... [progress:{},total:{},{}%]", progress, total, progress*100/total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                raf.close();
                if(future.isSuccess()) {
                    logger.info("completed");
                }
            }
        });
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("HttpZeroCopyDownloadHandler error", cause);
    }
}