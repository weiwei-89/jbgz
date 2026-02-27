package cn.tj.food.common.tcp;

public abstract class ClientSessionAdapter<CNT> implements ClientSession<CNT> {
    protected final ClientSession<CNT> session;

    public ClientSessionAdapter(ClientSession<CNT> session) {
        this.session = session;
    }

    @Override
    public CNT connect(Config config) throws Exception {
        try {
            return this.session.connect(config);
        } finally {
            this.connectDone(config);
        }
    }

    @Override
    public boolean isActive() {
        return this.session.isActive();
    }

    @Override
    public SessionFuture send(String info) throws Exception {
        return this.session.send(info);
    }

    @Override
    public void read(String info) throws Exception {
        this.session.read(info);
    }

    @Override
    public void close() throws Exception {
        try {
            this.session.close();
        } finally {
            this.closeDone();
        }
    }

    protected abstract void connectDone(Config config) throws Exception;

    protected abstract void closeDone() throws Exception;
}