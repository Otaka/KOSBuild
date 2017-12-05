package com.asyncsockets;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author sad
 */
public class ListenableFutureTaskWithData<T> extends ListenableFutureTask {

  

    public ListenableFutureTaskWithData() {
        super(createEmptyCallable());
    }

    public ListenableFutureTaskWithData(Callback<T> onFinish) {
        super(createEmptyCallable(), onFinish);
    }

    public ListenableFutureTaskWithData(Callback<T> onFinish, Callback<Throwable> onError) {
        super(createEmptyCallable(), onFinish, onError);
    }

    public void finishFutureAndReturnException(Throwable returnData) {
        set(returnData);
    }

    public void finishFutureAndReturnData(T returnData) {
        set(returnData);
    }

    @Override
    public void run() {
        super.run();
    }

    private static Callable createEmptyCallable() {
        return (Callable) () -> {
            throw new UnsupportedOperationException("Should not be executed");
        };
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        Object value= super.get();
        if(value !=null && value instanceof Throwable){
            throw new ExecutionException((Throwable) value);
        }
        return (T) value;
    }
    
    
}
