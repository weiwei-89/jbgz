package cn.tj.food.netty_ext.handler;

import cn.tj.food.common.router.ApiLoader;
import cn.tj.food.netty_ext.Connector;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HttpGetHandler extends MessageToMessageDecoder<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpGetHandler.class);

    private final ApiLoader apiLoader;

    public HttpGetHandler(ApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("HttpGetHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        logger.info("params: {}", JSON.toJSONString(params));
        Object result = this.apiLoader.form(decoder.path(), params);
        logger.info("result: {}", JSON.toJSONString(result));
        if(result instanceof Connector) {
            ctx.pipeline()
                    .addAfter("HttpGetHandler", "HttpDownloadHandler", new HttpDownloadHandler());
            out.add(result);
        } else {
            if(result == null) {
                out.add("");
            } else {
                out.add(result);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("HttpGetHandler error", cause);
    }
}