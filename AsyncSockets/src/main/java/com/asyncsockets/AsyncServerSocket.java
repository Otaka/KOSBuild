package com.asyncsockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author sad
 */
public class AsyncServerSocket {

    private int port;
    private Thread serverAcceptThread;
    private ServerSocket serverSocket;
    private SocketsManager socketManager;

    AsyncServerSocket(SocketsManager socketManager, int port) {
        this.port = port;
        this.socketManager = socketManager;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        serverAcceptThread = new Thread() {
            @Override
            public void run() {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket newSocket = serverSocket.accept();
                        acceptSocket(newSocket);
                    } catch (SocketException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        serverAcceptThread.setDaemon(true);
        serverAcceptThread.setName("serverAcceptThread");
        serverAcceptThread.start();
    }

    public void setConnectionEvent(ConnectionEvent connectionEvent) {
        socketManager.addConnectionEvent(connectionEvent);
    }

    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private void acceptSocket(Socket socket) throws IOException {
        socketManager.acceptSocket(socket, true);
    }
    
}
