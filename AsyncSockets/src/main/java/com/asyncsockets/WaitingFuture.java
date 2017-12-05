package com.asyncsockets;


/**
 * @author sad
 */
public class WaitingFuture {

    private int responseId;
    private ListenableFutureTaskWithData future;
    private long timestamp;
    private long timeout;

    public WaitingFuture(int responseId, ListenableFutureTaskWithData future, long timeout) {
        timestamp = System.currentTimeMillis();
        this.responseId = responseId;
        this.future = future;
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ListenableFutureTaskWithData getFuture() {
        return future;
    }

    public int getResponseId() {
        return responseId;
    }

}
