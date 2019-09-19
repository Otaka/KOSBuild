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
        System.out.println("Open serverSocker "+port);
        serverAcceptThread = new Thread() {
            @Override
            public void run() {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket newSocket = serverSocket.accept();
                        acceptSocket(newSocket);
                    } catch (SocketException ex) {
                        if(!SocketsManager.isSocketClosedException(ex)){
                            ex.printStackTrace();
                        }

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

    public int getPort() {
        return port;
    }
    
    

    public void setConnectionEvent(ConnectionEvent connectionEvent) {
        socketManager.addConnectionEvent(connectionEvent);
    }

    public void stop() throws IOException {
        if (serverSocket != null) {
            System.out.println("Close server socket");
            serverSocket.close();
        }
    }

    private void acceptSocket(Socket socket) throws IOException {
        System.out.println("Accept socket from "+socket.getInetAddress());
        socketManager.acceptSocket(socket, true);
    }
    
}
