package com.asyncsockets;



/**
 * @author sad
 */
public interface Callback<T> {
    public void complete(T result);
}
