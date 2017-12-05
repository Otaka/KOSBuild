package com.asyncsockets;


/**
 * @author sad
 */
public abstract class ConnectionEvent {
    public abstract void clientConnected(SocketHandler socketHandler);
    public abstract void clientDisconnected(SocketHandler socketHandler);
}
