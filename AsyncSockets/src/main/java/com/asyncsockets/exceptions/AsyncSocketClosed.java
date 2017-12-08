package com.asyncsockets.exceptions;

/**
 * @author sad
 */
public class AsyncSocketClosed extends RuntimeException {

    public AsyncSocketClosed(String message) {
        super(message);
    }

}
