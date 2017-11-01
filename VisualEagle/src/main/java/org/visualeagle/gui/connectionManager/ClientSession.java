package org.visualeagle.gui.connectionManager;

import java.io.IOException;
import java.net.Socket;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class ClientSession extends AbstractSession {

    private String host;
    private int port;

    public ClientSession(String host) {
        host=host.trim();
        if (!host.contains(":")) {
            throw new IllegalArgumentException("Host name should be in host:port format, but found [" + host + "]");
        }

        port = Integer.parseInt(host.substring(host.indexOf(":") + 1).trim());
        this.host = host.substring(0, host.indexOf(":")).trim();
    }

    @Override
    public void closeConnection() throws IOException {

    }

    @Override
    public boolean aquireSocket() throws IOException {
        connection = new Socket(host, port);
        inputStream = IOUtils.buffer(connection.getInputStream());
        outputStream = IOUtils.buffer(connection.getOutputStream(), 1024);
        doSimpleValidation();
        return true;
    }

    private boolean doSimpleValidation() throws IOException {
        int val1 = receiveInt();
        int val2 = receiveInt();

        int result = val1 / 2 + val2 / 2;
        sendInt(result);

        String resultString = receiveString();
        if (resultString.equals("1")) {
            return true;
        }
        connection.close();
        return false;
    }

}
