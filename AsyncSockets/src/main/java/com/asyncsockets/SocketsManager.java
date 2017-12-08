package com.asyncsockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author sad
 */
public class SocketsManager {

    private List<SocketHandler> socketHandlers = new ArrayList<>();
    private Thread socketThread;
    private List<SocketHandler> socketsToAdd = Collections.synchronizedList(new ArrayList<>());
    private List<ConnectionEvent> connectionEvents = new ArrayList<>();
    static Executor eventsExecutor = Executors.newCachedThreadPool();
    private List<AsyncClientSocket> clientSocketsScheduledForReconnection = Collections.synchronizedList(new ArrayList<>());

    public SocketsManager() {
        eventsExecutor = Executors.newCachedThreadPool((Runnable r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public void addConnectionEvent(ConnectionEvent connectionEvent) {
        this.connectionEvents.add(connectionEvent);
    }

    public AsyncServerSocket createServerSocket(int port) {
        AsyncServerSocket serverSocket = new AsyncServerSocket(this, port);
        return serverSocket;
    }

    public AsyncClientSocket createClientSocket(InetAddress inetAddress, int port) throws IOException {
        return createClientSocket(inetAddress, port, null, -1);
    }

    public AsyncClientSocket createClientSocket(InetAddress inetAddress, int port, int autoreconnectTimeout) throws IOException {
        return createClientSocket(inetAddress, port, null, autoreconnectTimeout);
    }

    public AsyncClientSocket createClientSocket(InetAddress inetAddress, int port, ConnectionEvent connectionEvent, int autoreconnectTimeout) throws IOException {
        Socket socket = null;
        SocketHandler socketHandler = null;
        try {
            socket = new Socket(inetAddress, port);
            socketHandler = new SocketHandler(socket);
            socketHandler.setBelongToServer(false);
        } catch (IOException ex) {
            if (autoreconnectTimeout <= 0) {
                throw ex;//exit immidiately
            }
        }

        AsyncClientSocket clientSocket = new AsyncClientSocket(socketHandler, inetAddress, port, autoreconnectTimeout);
        clientSocket.setConnectionEvent(connectionEvent);
        if (socketHandler != null) {
            socketsToAdd.add(socketHandler);
        } else {
            clientSocket.setCrashed(true);
            clientSocket.setLastReconnectTimestamp(System.currentTimeMillis());
        }

        if (autoreconnectTimeout > 0) {
            clientSocketsScheduledForReconnection.add(clientSocket);
        }

        return clientSocket;
    }

    SocketHandler acceptSocket(Socket socket, boolean belongToServer) throws IOException {
        SocketHandler socketHandler = new SocketHandler(socket);
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
        socketThread.setPriority(Thread.MIN_PRIORITY + 1);
        socketThread.start();
    }

    private void socketThreadBody() {
        while (!Thread.interrupted()) {
            processSockets();
        }
    }
    int totalCount;
    int sleepCount;
    long lastProcessTime = System.currentTimeMillis();

    private void processSockets() {
        addWaitingSockets();
        boolean emptyLoop = true;
        for (int i = 0; i < socketHandlers.size(); i++) {
            try {
                SocketHandler sh = socketHandlers.get(i);
                if (sh.process()) {
                    emptyLoop = false;
                    lastProcessTime = System.currentTimeMillis();
                }
            } catch (IOException ex) {
                if (isSocketClosedException(ex)) {
                    SocketHandler sh = socketHandlers.get(i);
                    closeSocket(sh);
                    socketHandlers.remove(i);
                    i--;
                } else {
                    ex.printStackTrace();
                }
            }
        }

        processReconnection();
        if (emptyLoop) {
            if (socketHandlers.isEmpty()) {
                sleep(500);
            } else {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastProcessTime) > 1) {
                    sleep(2);
                }
            }
        }
    }

    private long lastCheckOfReconnection = 0;

    private void processReconnection() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastCheckOfReconnection) > 1000) {
            lastCheckOfReconnection = currentTime;
            if (!clientSocketsScheduledForReconnection.isEmpty()) {
                for (int i = 0; i < clientSocketsScheduledForReconnection.size(); i++) {
                    AsyncClientSocket clientSocket = clientSocketsScheduledForReconnection.get(i);
                    if (clientSocket.isCrashed()) {
                        if ((currentTime - clientSocket.getLastReconnectTimestamp()) >= clientSocket.getAutoreconnectTimeInterval()) {
                            tryToReconnectClientSocket(clientSocket);
                        }
                    }
                }
            }
        }
    }

    private void tryToReconnectClientSocket(AsyncClientSocket clientSocket) {
        try {
            Socket socket = new Socket(clientSocket.getInetAddress(), clientSocket.getPort());
            SocketHandler socketHandler = new SocketHandler(socket);
            socketHandler.setBelongToServer(false);
            socketHandler.setOwner(clientSocket);
            socketHandler.setDataArrivedCallback(clientSocket.getDataEvent());
            clientSocket.setSocketHandler(socketHandler);
            clientSocket.setCrashed(false);
            clientSocket.setLastReconnectTimestamp(System.currentTimeMillis());
            socketsToAdd.add(socketHandler);
        } catch (IOException ex) {
            clientSocket.setLastReconnectTimestamp(System.currentTimeMillis());
        }

    }

    private void closeSocket(SocketHandler socketHandler) {
        try {
            for (ConnectionEvent ce : connectionEvents) {
                eventsExecutor.execute(() -> {
                    ce.clientDisconnected(socketHandler);
                });
            }
            if (socketHandler.getConnectionEvent() != null) {
                eventsExecutor.execute(() -> {
                    socketHandler.getConnectionEvent().clientDisconnected(socketHandler);
                });
            }
            if (socketHandler.getOwner() != null && socketHandler.getOwner() instanceof AsyncClientSocket) {
                AsyncClientSocket asyncClient = (AsyncClientSocket) socketHandler.getOwner();
                if (asyncClient.getConnectionEvent() != null) {
                    eventsExecutor.execute(() -> {
                        asyncClient.getConnectionEvent().clientDisconnected(socketHandler);
                    });
                }
                asyncClient.setSocketHandler(null);
                asyncClient.setLastReconnectTimestamp(System.currentTimeMillis());
                asyncClient.setCrashed(true);
            }

            socketHandler.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSocketClosedException(Throwable thr) {
        if (thr instanceof SocketException) {
            String message = thr.getMessage();
            if (message == null) {
                return false;
            }
            if (message.contains("Connection reset by peer")) {
                return true;
            }
        }

        return false;
    }

    private void addWaitingSockets() {
        if (!socketsToAdd.isEmpty()) {
            while (!socketsToAdd.isEmpty()) {
                SocketHandler handler = socketsToAdd.remove(0);
                if (handler != null) {
                    socketHandlers.add(handler);
                    if (handler.isBelongToServer()) {
                        if (!connectionEvents.isEmpty()) {
                            eventsExecutor.execute(() -> {
                                for (ConnectionEvent ce : connectionEvents) {
                                    ce.clientConnected(handler);
                                }
                            });
                        }
                    }

                    if (handler.getConnectionEvent() != null) {
                        eventsExecutor.execute(() -> {
                            handler.getConnectionEvent().clientConnected(handler);
                        });
                    }
                    if (handler.getOwner() != null && handler.getOwner() instanceof AsyncClientSocket) {
                        AsyncClientSocket asyncClient = (AsyncClientSocket) handler.getOwner();
                        if (asyncClient.getConnectionEvent() != null) {
                            asyncClient.getConnectionEvent().clientConnected(handler);
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
