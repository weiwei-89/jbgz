package cn.tj.food.netty_ext.handler;

import cn.tj.food.common.router.ApiLoader;
import cn.tj.food.netty_ext.util.ByteBufUtil;
import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HttpFormHandler extends MessageToMessageDecoder<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpFormHandler.class);

    private final ApiLoader apiLoader;

    public HttpFormHandler(ApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("HttpFormHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        String uri = ctx.channel().attr(HttpDispatchHandler.CONTEXT_URI).get();
        ByteBuf content = request.content();
        String body = new String(ByteBufUtil.getReadableBytes(content));
        QueryStringDecoder decoder = new QueryStringDecoder(body, false);
        Map<String, List<String>> params = decoder.parameters();
        logger.info("params: {}", JSON.toJSONString(params));
        Object result = this.apiLoader.form(uri, params);
        logger.info("result: {}", JSON.toJSONString(result));
        if(result == null) {
            out.add("");
        } else {
            out.add(result);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("HttpFormHandler error", cause);
    }
}