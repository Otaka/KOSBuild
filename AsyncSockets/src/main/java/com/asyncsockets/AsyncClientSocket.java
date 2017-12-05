package com.asyncsockets;

import java.net.InetAddress;

/**
 * @author sad
 */
public class AsyncClientSocket {

    private SocketsManager socketsManager;
    private DataEvent dataEvent;
    private InetAddress inetAddress;
    private int port;
    private SocketHandler socketHandler;

    AsyncClientSocket(SocketHandler socketHandler, SocketsManager socketsManager, InetAddress inetAddress, int port) {
        this.socketsManager = socketsManager;
        this.inetAddress = inetAddress;
        this.port = port;
        this.socketHandler = socketHandler;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    public void setDataEvent(DataEvent dataEvent) {
        socketHandler.setDataArrivedCallback(dataEvent);
    }

}
