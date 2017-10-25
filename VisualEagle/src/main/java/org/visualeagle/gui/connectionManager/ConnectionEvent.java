package org.visualeagle.gui.connectionManager;

/**
 * @author sad
 */
public interface ConnectionEvent {
    public void connected();
    public void connecting();
    public void disconnected();
}
