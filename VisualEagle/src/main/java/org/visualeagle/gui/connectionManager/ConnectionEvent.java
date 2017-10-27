package org.visualeagle.gui.connectionmanager;

/**
 * @author sad
 */
public interface ConnectionEvent {
    public void connected();
    public void connecting();
    public void disconnected();
}
