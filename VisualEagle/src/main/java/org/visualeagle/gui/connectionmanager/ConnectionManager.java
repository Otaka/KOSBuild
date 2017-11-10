package org.visualeagle.gui.connectionmanager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.visualeagle.utils.BaseActor;
import org.visualeagle.utils.ActorMessage;
import org.visualeagle.utils.MessageProcessedEvent;

/**
 * @author sad
 */
public class ConnectionManager extends BaseActor<ActorMessage> {

    private AbstractSession session;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private Timer checkAliveTimer = new Timer(true);

    public ConnectionManager() {
        schedulePingTimer();
    }

    private void schedulePingTimer() {
        checkAliveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendPingMessage();
            }
        }, 4000, 4000);
    }

    public ConnectionStatus getConnectionStatus() {
        if (session != null) {
            if (session.isConnected()) {
                return ConnectionStatus.CONNECTED;
            }
        }

        return connectionStatus;
    }

    public String getStatusMessage() {
        if (session == null) {
            return "no session";
        }

        return session.getStatusMessage();
    }

    public AbstractSession getSession() {
        return session;
    }

    @Override
    protected Object processMessage(ActorMessage message) throws Exception {
        switch (message.getCommand()) {
            case "ping":
                return ping();
            case "close":
                closeConnection();
                break;
            case "create_server":
                return createServer((CreateServerMessage) message.getData());
            case "connect_to_client":
                return connectToClient((ConnectToServerMessage) message.getData());
        }

        return null;
    }

    private void sendPingMessage() {
        if (connectionStatus == ConnectionStatus.CONNECTED && session != null && session.isConnected()) {
            sendMessage(new ActorMessage("ping"), (MessageProcessedEvent) (Object originalMessage, Object result, Throwable exception) -> {
                if (exception != null) {
                    try {
                        closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }, false, 5000);
        }
    }

    private String ping() throws IOException {
        if (connectionStatus == ConnectionStatus.CONNECTED && session != null && session.isConnected()) {
            session.sendString("PING");
            return session.receiveString();
        }
        return null;
    }

    private void closeConnection() throws IOException {
        if (session != null) {
            connectionStatus = ConnectionStatus.CONNECTING;
            session.closeConnection();
            connectionStatus = ConnectionStatus.DISCONNECTED;
            session = null;
        }
    }

    private String createServer(CreateServerMessage message) throws Exception {
        ServerSession serverSession = new ServerSession(message.port);
        serverSession.initConnection();
        session = serverSession;
        connectionStatus = ConnectionStatus.CONNECTING;
        return "Server created";
    }

    private String connectToClient(ConnectToServerMessage message) throws Exception {
        connectionStatus = ConnectionStatus.CONNECTING;
        ClientSession clientSession = new ClientSession(message.hostname);
        try {
            clientSession.initConnection();
        } catch (Exception ex) {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            throw ex;
        }
        session = clientSession;
        connectionStatus = ConnectionStatus.CONNECTED;
        return "Connected to server";
    }

    public static class CreateServerMessage {

        public int port;

        public CreateServerMessage(int port) {
            this.port = port;
        }
    }

    public static class ConnectToServerMessage {

        public String hostname;

        public ConnectToServerMessage(String hostname) {
            this.hostname = hostname;
        }
    }
}
