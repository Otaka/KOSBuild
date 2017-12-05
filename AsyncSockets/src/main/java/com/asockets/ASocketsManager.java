package com.asockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author sad
 */
public class ASocketsManager {

    private List<ASocketHandler> socketHandlers = new ArrayList<>();
    private Thread socketThread;
    private List<ASocketHandler> socketsToAdd = Collections.synchronizedList(new ArrayList<>());
    private ConnectionEvent connectionEvent;
    private static Executor eventsExecutor = Executors.newCachedThreadPool();

    public ASocketsManager() {
        eventsExecutor = Executors.newCachedThreadPool((Runnable r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public void setConnectionEvent(ConnectionEvent connectionEvent) {
        this.connectionEvent = connectionEvent;
    }

    public AServerSocket createServerSocket(int port) {
        AServerSocket serverSocket = new AServerSocket(this, port);
        return serverSocket;
    }

    public AClientSocket createClientSocket(InetAddress inetAddress, int port) throws IOException {
        Socket socket = new Socket(inetAddress, port);
        ASocketHandler socketHandler = acceptSocket(socket, false);
        AClientSocket clientSocket = new AClientSocket(socketHandler, this, inetAddress, port);
        return clientSocket;
    }

    ASocketHandler acceptSocket(Socket socket, boolean belongToServer) throws IOException {
        ASocketHandler socketHandler = new ASocketHandler(socket);
        socketHandler.setBelongToServer(belongToServer);
        socketsToAdd.add(socketHandler);
        return socketHandler;
    }

    public void start() {
        socketThread = new Thread() {
            @Override
            public void run() {
                socketThreadBody();
            }
        };

        socketThread.setName("SocketsProcessingThread");
        socketThread.setDaemon(true);
        socketThread.start();
    }

    private void socketThreadBody() {
        while (!Thread.interrupted()) {
            processSockets();
        }
    }

    private void processSockets() {
        addWaitingSockets();
        boolean emptyLoop = true;
        long lastProcessTime = System.currentTimeMillis();

        for (int i = 0; i < socketHandlers.size(); i++) {
            try {
                ASocketHandler sh = socketHandlers.get(i);
                if (sh.process()) {
                    emptyLoop = false;
                    lastProcessTime = System.currentTimeMillis();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (emptyLoop) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastProcessTime > 1) {
                sleep(2);
            }
        }
    }

    private void addWaitingSockets() {
        if (!socketsToAdd.isEmpty()) {
            while (!socketsToAdd.isEmpty()) {
                ASocketHandler handler = socketsToAdd.remove(0);
                if (handler != null) {
                    socketHandlers.add(handler);
                    if (handler.isBelongToServer()) {
                        if (connectionEvent != null) {
                            eventsExecutor.execute(() -> {
                                connectionEvent.clientConnected(handler);
                            });

                        }
                    }
                }
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
