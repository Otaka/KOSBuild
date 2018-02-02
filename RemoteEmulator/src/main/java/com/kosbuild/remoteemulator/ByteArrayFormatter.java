package com.kosbuild.remoteemulator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author sad
 */
public class ByteArrayFormatter {

    public ByteArrayOutputStream outputStream;

    public ByteArrayFormatter(int estimatedSize) {
        outputStream = new ByteArrayOutputStream(estimatedSize);
    }

    public ByteArrayFormatter() {
        this(100);
    }

    public void sendString(String string) throws IOException {
        byte[] buffer = string.getBytes(StandardCharsets.UTF_8);
        sendInt(buffer.length);
        sendRaw(buffer);
        outputStream.flush();
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

    private byte[] toByteArray(int value) {
        return new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value};
    }

    private static byte[] toByteArrayLong(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public void sendRaw(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }

}
