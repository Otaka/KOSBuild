package org.visualeagle.gui.remoteconnection;

import com.asyncsockets.SocketHandler;

/**
 * @author sad
 */
public interface ConnectionStatusChangedEvent {
    public void clientConnected(SocketHandler socketHandler);
    public void clientDisconnected();
    public void serverStarted();
    public void serverStopped();
    public void error(Throwable thr);
}
