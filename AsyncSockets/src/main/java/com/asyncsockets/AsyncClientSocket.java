package com.asyncsockets;

import java.net.InetAddress;

/**
 * @author sad
 */
public class AsyncClientSocket {


    private InetAddress inetAddress;
    private int port;
    private SocketHandler socketHandler;

    AsyncClientSocket(SocketHandler socketHandler,InetAddress inetAddress, int port) {
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
