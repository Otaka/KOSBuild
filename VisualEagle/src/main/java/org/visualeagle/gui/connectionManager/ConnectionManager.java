package org.visualeagle.gui.connectionmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.visualeagle.gui.connectionManager.AbstractSession;

/**
 * @author sad
 */
public class ConnectionManager {

    private final List<ConnectionEvent> events = new ArrayList<>();
    private boolean connected = false;
    private AbstractSession session;

    public void setSession(AbstractSession session) throws IOException {
        this.session = session;
        if (session != null) {
            connect();
        } else {
            closeConnection();
        }
    }

    public void addConnectionListener(ConnectionEvent event) {
        events.add(event);
    }

    public void removeConnectionListener(ConnectionEvent event) {
        events.remove(event);
    }

    public void changeConnectionStatus(boolean connected) {
        for (ConnectionEvent event : events) {
            if (connected) {
                event.connected();
            } else {
                event.disconnected();
            }
        }
    }

    public AbstractSession getSession() {
        return session;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect() {
        connected = true;
        changeConnectionStatus(connected);
    }

    public void closeConnection() throws IOException {
        if (session != null) {
            session.closeConnection();
            session = null;
        }
        connected = false;
        changeConnectionStatus(connected);
    }
}
