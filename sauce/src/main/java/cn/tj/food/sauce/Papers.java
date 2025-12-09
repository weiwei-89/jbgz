package cn.tj.food.sauce;

import cn.tj.food.sauce.xml.model.Protocol;

import java.util.HashMap;

public class Papers extends HashMap<String, Protocol> {
    private final String protocolId;

    public Papers(int size, String protocolId) {
        super(size);
        this.protocolId = protocolId;
    }

    public String getProtocolId() {
        return this.protocolId;
    }

    private int maxLength;

    public int getMaxLength() {
        return this.maxLength;
    }
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}