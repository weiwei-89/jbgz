package cn.tj.food.common.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompleteFuture implements SessionFuture {
    private static final Logger logger = LoggerFactory.getLogger(CompleteFuture.class);

    private volatile boolean complete = false;
    private volatile boolean error = false;
    private volatile Throwable cause = null;
    private FutureListener listener;

    @Override
    public synchronized void addListener(FutureListener listener) {
        this.listener = listener;
        if(this.complete) {
            this.notifyListener();
        }
        if(this.error) {
            this.notifyErrorListener();
        }
    }

    public synchronized void complete() {
        this.complete = true;
        this.notifyListener();
    }

    public synchronized void error(Throwable cause) {
        this.error = true;
        this.cause = cause;
        this.notifyErrorListener();
    }

    private void notifyListener() {
        if(this.listener == null) {
            return;
        }
        try {
            this.listener.onComplete();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void notifyErrorListener() {
        if(this.listener == null) {
            return;
        }
        try {
            this.listener.onError(this.cause);
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}