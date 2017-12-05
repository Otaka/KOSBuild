package com.asyncsockets;

import java.io.IOException;

/**
 * @author sad
 */
public abstract class DataEvent {
    public abstract void dataArrived(SocketHandler socket, Request request) throws IOException;
}
