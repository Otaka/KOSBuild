package com.kosbuild.remoteemulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public abstract class AbstractSession {

    protected Socket connection;

    public abstract void closeConnection() throws IOException;

    public abstract boolean aquireSocket() throws IOException;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    public void initConnection() throws Exception {
        if (!aquireSocket()) {
            throw new RuntimeException("Cannot aquire socket");
        }

        inputStream = IOUtils.buffer(connection.getInputStream());
        outputStream = IOUtils.buffer(connection.getOutputStream(), 1024);
    }

    public void sendRaw(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    public byte[] receive(int count) throws IOException {
        byte[] buffer = new byte[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = (byte) inputStream.read();
        }
        return buffer;
    }

    public void sendString(String string) throws IOException {
        byte[] buffer = string.getBytes(StandardCharsets.UTF_8);
        sendInt(buffer.length);
        sendRaw(buffer);
        outputStream.flush();
    }

    public boolean receiveBoolean() throws IOException {
        return receiveInt() == 1;
    }

    public int receiveInt() throws IOException {
        return fromByteArray(receive(4));
    }

    public long receiveLong() throws IOException {
        return fromByteArrayLong(receive(8));
    }

    public void sendBoolean(boolean value) throws IOException {
        sendInt(value ? 1 : 0);
    }

    public void sendInt(int value) throws IOException {
        sendRaw(toByteArray(value));
        outputStream.flush();
    }

    public void sendLong(long value) throws IOException {
        sendRaw(toByteArrayLong(value));
        outputStream.flush();
    }

    public String receiveString() throws IOException {
        int sizeOfBuffer = receiveInt();
        return new String(receive(sizeOfBuffer), StandardCharsets.UTF_8);
    }

    public byte[] toByteArray(int value) {
        return new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value};
    }

    public int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte[] toByteArrayLong(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long fromByteArrayLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public void closeWithSendingMessage(String message) throws IOException {
        sendString("0");
        sendString(message);
        closeConnection();
    }

}
