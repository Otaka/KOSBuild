package org.visualeagle.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author sad
 */
public class AsyncServerSocket {

    private int port;
    private ServerSocket serverSocket;
    private Socket lastSocket;
    private Thread thread;
    private OnNewClientEvent newClientEvent;

    public AsyncServerSocket(int port, OnNewClientEvent newClientEvent) {
        this.port = port;
        this.newClientEvent = newClientEvent;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        thread = new Thread(() -> {
            while (serverSocket != null && serverSocket.isBound() && !thread.isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (lastSocket != null && !lastSocket.isClosed()) {
                        socket.close();
                    }
                    newClientEvent.onNewClient(socket);
                    lastSocket = socket;
                } catch (SocketException ex) {
                    if (!ex.getMessage().contains("socket closed")) {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        thread.setName("Async server socket thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if (lastSocket != null) {
            try {
                lastSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            lastSocket = null;
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = null;
        }
        thread.interrupt();
        thread = null;
    }

    public static interface OnNewClientEvent {

        public void onNewClient(Socket socket);
    }
}
