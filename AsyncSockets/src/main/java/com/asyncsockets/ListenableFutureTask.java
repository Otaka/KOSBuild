package com.asyncsockets;


import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author sad
 */
public class ListenableFutureTask<T> extends FutureTask<T> {

    private Callback<T> onFinish;
    private Callback<Throwable> onError;

    public ListenableFutureTask(Callable callable) {
        super(callable);
    }

    public ListenableFutureTask(Callable callable, Callback<T> onFinish) {
        super(callable);
        this.onFinish = onFinish;
    }

    public ListenableFutureTask(Callable callable, Callback<T> onFinish, Callback<Throwable> onError) {
        super(callable);
        this.onFinish = onFinish;
        this.onError = onError;
    }

    @Override
    protected void done() {
        super.done();
        try {
            Object result = get();
            if (result == null || !(result instanceof Throwable)) {
                if (onFinish != null) {
                    onFinish.complete((T) result);
                }
            } else {
                if (onError != null) {
                    onError.complete((Throwable) result);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
