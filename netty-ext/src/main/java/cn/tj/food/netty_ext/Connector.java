package cn.tj.food.netty_ext;

import cn.tj.food.common.router.ApiParam;

public class Connector {
    public enum TRANSFER_MODE {
        COMMON,
        ZERO_COPY
    }

    public enum DOWNLOAD_MODE {
        DOWNLOAD,
        PREVIEW
    }

    private TRANSFER_MODE transferMode = TRANSFER_MODE.COMMON;
    private DOWNLOAD_MODE downloadMode = DOWNLOAD_MODE.DOWNLOAD;
    private Object data;
    private ApiParam param;

    public Connector setTransferMode(TRANSFER_MODE transferMode) {
        this.transferMode = transferMode;
        return this;
    }
    public TRANSFER_MODE getTransferMode() {
        return this.transferMode;
    }
    public Connector setDownloadMode(DOWNLOAD_MODE downloadMode) {
        this.downloadMode = downloadMode;
        return this;
    }
    public DOWNLOAD_MODE getDownloadMode() {
        return this.downloadMode;
    }
    public Connector setParam(ApiParam param) {
        this.param = param;
        return this;
    }
    public ApiParam getParam() {
        return this.param;
    }
}