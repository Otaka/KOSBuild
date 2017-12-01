package org.visualeagle.gui.connectionmanager;

import java.io.IOException;
import java.net.Socket;
import org.apache.commons.io.IOUtils;
import org.visualeagle.utils.AsyncServerSocket;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class ServerSession extends AbstractSession {

    private AsyncServerSocket serverSocket;
    private int port;
    private String statusMessage;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public ServerSession(int port) {
        this.port = port;
    }

    @Override
    public void closeCurrentConnection() throws IOException {
        try {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            statusMessage = "Connection closing";
            if (connection != null) {
                connection.close();
            }
            statusMessage = "Connection closed";
            Lookup.get().put("connectedClient", Boolean.FALSE);
        } catch (Exception ex) {
            ex.printStackTrace();
            statusMessage = "Exception while closing [" + ex.getMessage() + "]";
        }

        connectionStatus = ConnectionStatus.CONNECTING;
        statusMessage = "Waiting for new connection";
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public boolean aquireSocket() throws IOException {
        connectionStatus = ConnectionStatus.CONNECTING;
        statusMessage = "Waiting for incoming connection";
        serverSocket = new AsyncServerSocket(port, (Socket socket) -> {
            try {
                connection = socket;
                inputStream = IOUtils.buffer(socket.getInputStream());
                outputStream = IOUtils.buffer(socket.getOutputStream(), 1024);
                
                if (!doSimpleValidation()) {
                    statusMessage = "Connected client [" + socket.getInetAddress() + "] but failed validation";
                    connectionStatus = ConnectionStatus.DISCONNECTED;
                    closeWithSendingMessage("Authentication error");
                } else {
                    sendString("1");
                    connectionStatus = ConnectionStatus.CONNECTED;
                    statusMessage = "Connected client [" + socket.getInetAddress() + "]";
                    Lookup.get().put("connectedClient", Boolean.TRUE);
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
