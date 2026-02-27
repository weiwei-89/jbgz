package cn.tj.food.netty_ext.handler;

import cn.tj.food.common.router.ApiLoader;
import cn.tj.food.common.router.ApiParam;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpDispatchHandler.class);

    public static final AttributeKey<String> CONTEXT_URI = AttributeKey.newInstance("uri");
    public static final AttributeKey<ApiParam> CONTEXT_PARAM = AttributeKey.newInstance("param");

    private final ApiLoader apiLoader;

    public HttpDispatchHandler(ApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("HttpDispatchHandler added");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        logger.info("uri: {}", uri);
        ctx.channel().attr(CONTEXT_URI).set(uri);
        HttpMethod method = request.method();
        logger.info("method: {}", method);
        if(method == HttpMethod.POST) {
            String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            if(contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString())) {
                ctx.pipeline()
                        .addAfter(
                                "HttpDispatchHandler",
                                "HttpJsonHandler",
                                new HttpJsonHandler(this.apiLoader)
                        );
                ctx.fireChannelRead(request.retain());
                return;
            } else if(contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
                ctx.pipeline()
                        .addAfter(
                                "HttpDispatchHandler",
                                "HttpFormHandler",
                                new HttpFormHandler(this.apiLoader)
                        );
                ctx.fireChannelRead(request.retain());
                return;
            } else if(contentType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString())) {
                ctx.pipeline()
                        .addAfter(
                                "HttpDispatchHandler",
                                "HttpMultipartFormHandler",
                                new HttpMultipartFormHandler(this.apiLoader)
                        );
                ctx.fireChannelRead(request.retain());
                return;
            }
        } else if(method == HttpMethod.GET) {
            ctx.pipeline()
                    .addAfter(
                            "HttpDispatchHandler",
                            "HttpGetHandler",
                            new HttpGetHandler(this.apiLoader)
                    );
            ctx.fireChannelRead(request.retain());
            return;
        }
        throw new Exception("request not supported");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("HttpDispatchHandler error", cause);
    }
}