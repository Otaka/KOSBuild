package org.visualeagle.gui.connectionmanager;

import java.io.IOException;
import java.net.Socket;
import org.apache.commons.io.IOUtils;
import org.visualeagle.utils.AsyncServerSocket;

/**
 * @author sad
 */
public class ServerSession extends AbstractSession {

    private AsyncServerSocket serverSocket;
    private int port;
    private String statusMessage;
    private boolean connected = false;

    public ServerSession(int port) {
        this.port = port;
    }

    @Override
    public void closeConnection() throws IOException {
        try {
            connected = false;
            statusMessage = "Connection closing";
            serverSocket.stop();
            statusMessage = "Connection closed";
        } catch (Exception ex) {
            ex.printStackTrace();
            statusMessage = "Exception while closing [" + ex.getMessage() + "]";
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public boolean aquireSocket() throws IOException {
        connected = false;
        statusMessage = "Waiting for incoming connection";
        serverSocket = new AsyncServerSocket(port, (Socket socket) -> {
            try {
                connected = false;
                inputStream = IOUtils.buffer(socket.getInputStream());
                outputStream = IOUtils.buffer(socket.getOutputStream(), 1024);
                if (!doSimpleValidation()) {
                    statusMessage = "Connected client [" + socket.getInetAddress() + "] but failed validation";
                    closeWithSendingMessage("Authentication error");
                } else {
                    sendString("1");
                    connected = true;
                    statusMessage = "Connected client [" + socket.getInetAddress() + "]";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        serverSocket.start();

        return true;
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
