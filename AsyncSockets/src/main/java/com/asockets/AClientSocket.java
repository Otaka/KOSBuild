package com.asockets;

import java.net.InetAddress;

/**
 * @author sad
 */
public class AClientSocket {

    private ASocketsManager socketsManager;
    private DataEvent dataEvent;
    private InetAddress inetAddress;
    private int port;
    private ASocketHandler socketHandler;

    AClientSocket(ASocketHandler socketHandler, ASocketsManager socketsManager, InetAddress inetAddress, int port) {
        this.socketsManager = socketsManager;
        this.inetAddress = inetAddress;
        this.port = port;
        this.socketHandler = socketHandler;
    }

    public ASocketHandler getSocketHandler() {
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
