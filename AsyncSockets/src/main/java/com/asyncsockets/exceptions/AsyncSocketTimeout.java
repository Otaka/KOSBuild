package com.asyncsockets.exceptions;

/**
 * @author sad
 */
public class AsyncSocketTimeout extends RuntimeException {

    public AsyncSocketTimeout(String message) {
        super(message);
    }

}
