package org.visualeagle.gui.remoteconnection;

import com.asyncsockets.AsyncServerSocket;
import com.asyncsockets.ConnectionEvent;
import com.asyncsockets.ListenableFutureTaskWithData;
import com.asyncsockets.Request;
import com.asyncsockets.SocketHandler;
import com.asyncsockets.SocketsManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Settings;

/**
 * @author sad
 */
public class RemoteConnectionManager {
    private SocketsManager socketsManager;
    private AsyncServerSocket serverSocket;
    private SocketHandler currentConnection;
    private List<ConnectionStatusChangedEvent> connectionEvents = new ArrayList<>();
    private final static int HANDSHAKE_COMMAND = 1;

    public void init() throws IOException {
        socketsManager = new SocketsManager();
        socketsManager.start();

        int connectionPort = Settings.getIntProperty("connectionManager.port", -1);
        if (connectionPort == -1) {
            return;
        }

        createServer(connectionPort);
    }

    public int getServerPort() {
        if (serverSocket == null) {
            return -1;
        }
        return serverSocket.getPort();
    }

    public void addConnectionEvent(ConnectionStatusChangedEvent connectionEvent) {
        connectionEvents.add(connectionEvent);
    }

    public void removeConnectionEvent(ConnectionStatusChangedEvent connectionEvent) {
        connectionEvents.remove(connectionEvent);
    }

    public void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.stop();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            serverSocket = null;
        }

        for (ConnectionStatusChangedEvent ce : connectionEvents) {
            ce.serverStopped();
        }
        //close all already connected sockets
        for(int i=0;i<clients.size();i++){
            SocketHandler sh=clients.get(i);
            if(sh!=null){
                try {
                    sh.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        clients.clear();

        Settings.removeProperty("connectionManager.port");
        Settings.flush();
    }
    
    private ArrayList<SocketHandler>clients=new ArrayList<>();
    public void createServer(int serverPort) throws IOException {
        serverSocket = socketsManager.createServerSocket(serverPort);
        serverSocket.setConnectionEvent(new ConnectionEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                try {
                    System.out.println("Client connected");
                    clients.add(socketHandler);
                    int value1 = (int) (Math.random() * 9999);
                    int value2 = (int) (Math.random() * 9999);
                    String dataToSend = "" + value1 + "|" + value2;
                    ListenableFutureTaskWithData<Request> future = socketHandler.writeWithExpectingResult(HANDSHAKE_COMMAND, dataToSend.getBytes(StandardCharsets.UTF_8), -1, 10000, null, null);
                    Request result = future.get();
                    String response = result.getResponseAsString();
                    if (!response.equals("" + (value1 + value2))) {
                        System.out.println("Client not passed verification and disconnected");
                        socketHandler.close();
                        return;
                    }

                    currentConnection = socketHandler;
                    processOnClientConnectedEvents(socketHandler);
                    Lookup.get().put("connectedClient", true);
                    System.out.println("Client passed verification and connected");
                } catch (InterruptedException | ExecutionException | IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void clientDisconnected(SocketHandler socketHandler) {
                clients.remove(socketHandler);
                onClientDisconnected();
            }
        });

        for (ConnectionStatusChangedEvent ce : connectionEvents) {
            ce.serverStarted();
        }

        serverSocket.start();
    }

    private void processOnClientConnectedEvents(SocketHandler socketHandler) {
        for (ConnectionStatusChangedEvent ce : connectionEvents) {
            ce.clientConnected(socketHandler);
        }
    }

    private void onClientDisconnected() {
        if (currentConnection != null) {
            System.out.println("Client disconnected");
            Lookup.get().put("connectedClient", false);
            currentConnection = null;
            for (ConnectionStatusChangedEvent ce : connectionEvents) {
                ce.clientDisconnected();
            }
        }
    }

    public boolean isServerStarted() {
        return serverSocket != null;
    }

    public SocketHandler getCurrentConnection() {
        return currentConnection;
    }

    
    
    public boolean isConnectionEstablished() {
        
        return isServerStarted()==true && currentConnection != null;
    }
}
