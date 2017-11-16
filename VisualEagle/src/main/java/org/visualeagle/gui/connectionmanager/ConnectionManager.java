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

    private ServerSession session;
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
            return session.getConnectionStatus();
        }

        return ConnectionStatus.DISCONNECTED;
    }

    public String getStatusMessage() {
        if (session == null) {
            return "disconnected";
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

        }

        return null;
    }

    private void sendPingMessage() {
        if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
            sendMessage(new ActorMessage("ping"), (MessageProcessedEvent) (Object originalMessage, Object result, Throwable exception) -> {
                if (exception != null) {
                    try {
                        closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }, false, 3000);
        }
    }

    private String ping() throws IOException {
        if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
            session.sendString("PING");
            return session.receiveString();
        }
        return null;
    }

    private void closeConnection() throws IOException {
        if (session != null) {
            session.closeCurrentConnection();
        }
    }

    private String createServer(CreateServerMessage message) throws Exception {
        ServerSession serverSession = new ServerSession(message.port);
        serverSession.initConnection();
        session = serverSession;
        return "Server created";
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
