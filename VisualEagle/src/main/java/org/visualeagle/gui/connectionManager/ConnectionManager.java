package org.visualeagle.gui.connectionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class ConnectionManager {

    private final List<ConnectionEvent> events = new ArrayList<>();
    private boolean connected = false;

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

    public boolean isConnected() {
        return connected;
    }
    
    public void connect(){
        connected = true;
        changeConnectionStatus(connected);
    }

    public void closeConnection() {
        connected = false;
        changeConnectionStatus(connected);
    }
}
