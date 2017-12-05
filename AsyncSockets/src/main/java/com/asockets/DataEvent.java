package com.asockets;

import com.asyncsockets.*;
import java.io.IOException;

/**
 * @author sad
 */
public abstract class DataEvent {
    public abstract void dataArrived(ASocketHandler socket, Request request) throws IOException;
}
