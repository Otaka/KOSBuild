package com.asockets;

import com.asyncsockets.*;

/**
 * @author sad
 */
public abstract class ConnectionEvent {
    public abstract void clientConnected(ASocketHandler socketHandler);
    public abstract void clientDisconnected(ASocketHandler socketHandler);
}
