package org.visualeagle.utils;

/**
 * @author sad
 */
public interface MessageProcessedEvent<T> {
    void processed(T originalMessage, Object result, Throwable exception);
}
