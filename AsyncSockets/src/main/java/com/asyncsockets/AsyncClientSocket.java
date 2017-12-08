package com.asyncsockets;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author sad
 */
public class AsyncClientSocket {

    private InetAddress inetAddress;
    private int port;
    private SocketHandler socketHandler;
    private int autoreconnectTimeInterval;
    private boolean autoreconnectEnabled;
    private boolean crashed = false;
    private long lastReconnectTimestamp;
    private ConnectionEvent connectionEvent;
    private DataEvent dataEvent;

    AsyncClientSocket(SocketHandler socketHandler, InetAddress inetAddress, int port, int autoreconnectTimeInterval) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.socketHandler = socketHandler;
        if (socketHandler != null) {
            socketHandler.setOwner(this);
        }
        if (autoreconnectTimeInterval > 0) {
            this.autoreconnectTimeInterval = autoreconnectTimeInterval;
            this.autoreconnectEnabled = true;
        } else {
            this.autoreconnectEnabled = false;
        }
    }

    public void setConnectionEvent(ConnectionEvent connectionEvent) {
        this.connectionEvent = connectionEvent;
    }

    public ConnectionEvent getConnectionEvent() {
        return connectionEvent;
    }

    public long getLastReconnectTimestamp() {
        return lastReconnectTimestamp;
    }

    public void setLastReconnectTimestamp(long lastReconnectTimestamp) {
        this.lastReconnectTimestamp = lastReconnectTimestamp;
    }

    public int getAutoreconnectTimeInterval() {
        return autoreconnectTimeInterval;
    }

    public boolean isAutoreconnectEnabled() {
        return autoreconnectEnabled;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    void setSocketHandler(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    public void setDataEvent(DataEvent dataEvent) {
        this.dataEvent = dataEvent;
        if (socketHandler != null) {
            socketHandler.setDataArrivedCallback(dataEvent);
        }
    }

    public DataEvent getDataEvent() {
        return dataEvent;
    }
    
    

    public void close() throws IOException {
        socketHandler.close();
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public boolean isClosed() {
        return socketHandler.isClosed();
    }

    public ListenableFutureTask write(byte[] buffer, int responseForRequest, Callback onFinish, Callback onError) {
        return socketHandler.write(buffer, responseForRequest, onFinish, onError);
    }

    public ListenableFutureTaskWithData writeWithExpectingResult(byte[] buffer, int responseForRequest, long timeout, Callback onFinish, Callback onError) {
        return socketHandler.writeWithExpectingResult(buffer, responseForRequest, timeout, onFinish, onError);
    }

}
