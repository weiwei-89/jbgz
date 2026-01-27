package cn.tj.food.common.tcp;

public abstract class ClientCommonSession<CNT> implements ClientSession<CNT> {
    private CNT connection;

    public void init(Config config) throws Exception {
        this.connection = this.connect(config);
    }

    public CNT getConnection() {
        return this.connection;
    }
}