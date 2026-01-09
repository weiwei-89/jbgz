package cn.tj.food.netty_ext;

import cn.tj.food.common.router.ApiParam;

public class Connector {
    private Object data;
    private ApiParam param;

    public Connector setParam(ApiParam param) {
        this.param = param;
        return this;
    }
    public ApiParam getParam() {
        return this.param;
    }
}