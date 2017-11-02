package com.kosbuild.remoteemulator;

import java.io.IOException;
import java.net.ServerSocket;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class ServerSession extends AbstractSession {

    private ServerSocket serverSocket;
    private int port;

    public ServerSession(int port) {
        this.port = port;
    }

    @Override
    public void closeConnection() throws IOException {
        connection.close();
        serverSocket.close();
    }

    @Override
    public boolean aquireSocket() throws IOException {
        serverSocket = new ServerSocket(port);
        connection = serverSocket.accept();
        connection.setKeepAlive(true);
        System.out.println("Accepted new client "+connection.getInetAddress());
        inputStream = IOUtils.buffer(connection.getInputStream());
        outputStream = IOUtils.buffer(connection.getOutputStream(), 1024);
        if (!doSimpleValidation()) {
            closeWithSendingMessage("Authentication error");
            return false;
        } else {
            sendString("1");
            return true;
        }
    }

    private boolean doSimpleValidation() throws IOException {
        int val1 = (int) (Math.random() * Integer.MAX_VALUE);
        int val2 = (int) (Math.random() * Integer.MAX_VALUE);
        sendInt(val1);
        sendInt(val2);
        int expectedResult = val1 / 2 + val2 / 2;
        int result = receiveInt();
        return expectedResult == result;
    }
}
