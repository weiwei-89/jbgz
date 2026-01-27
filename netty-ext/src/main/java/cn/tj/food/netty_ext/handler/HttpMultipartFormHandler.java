package cn.tj.food.netty_ext.handler;

import cn.tj.food.common.router.ApiLoader;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpMultipartFormHandler extends MessageToMessageDecoder<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpMultipartFormHandler.class);

    private final ApiLoader apiLoader;

    public HttpMultipartFormHandler(ApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("HttpMultipartFormHandler added");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        String uri = ctx.channel().attr(HttpDispatchHandler.CONTEXT_URI).get();
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, request);
        try {
            List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();
            Map<String, Object> params = new HashMap<>(datas.size());
            InterfaceHttpData data = null;
            while(true) {
                data = decoder.next();
                if(data == null) {
                    break;
                }
                if(data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    params.put(attribute.getName(), attribute.getValue());
                    logger.info("param[{}]: {}", attribute.getName(), attribute.getValue());
                } else if(data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    params.put(fileUpload.getName(), fileUpload.get());
                    fileUpload.delete();
                }
            }
            Object result = this.apiLoader.multipartForm(uri, params);
            logger.info("result: {}", JSON.toJSONString(result));
            if(result == null) {
                out.add("");
            } else {
                out.add(result);
            }
        } finally {
            decoder.destroy();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("HttpMultipartFormHandler error", cause);
    }
}