package com.asyncsockets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author sad
 */
public class ListenableFutureTask<T> extends FutureTask<T> {

    private volatile Callback<T> onFinish;
    private volatile Callback<Throwable> onError;
    private volatile Callback<ProgressData> onProgress;

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

    public ListenableFutureTask(Callable callable, Callback<T> onFinish, Callback<Throwable> onError, Callback<ProgressData> onProgress) {
        super(callable);
        this.onFinish = onFinish;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public void progress(Object max, Object current) {
        if (onProgress != null) {
            SocketsManager.eventsExecutor.execute(() -> {
                onProgress.complete(new ProgressData(max, current));
            });
        }
    }

    public void setOnFinish(Callback<T> onFinish) {
        if (isDone()) {
            try {
                T result = get();
                if (result == null || !(result instanceof Throwable)) {
                    onFinish.complete(result);
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        } else {
            this.onFinish = onFinish;
        }
    }

    public void setOnError(Callback<Throwable> onError) {
        if (isDone()) {
            try {
                T result = get();
                if (result != null && (result instanceof Throwable)) {
                    onError.complete((Throwable) result);
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        } else {
            this.onError = onError;
        }
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
