package com.kosbuild.remoteemulator;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author sad
 */
public class ByteArrayParserFormatter {

    private ByteArrayInputStream inputStream;
    private ByteArrayFormatter formatter;
    public ByteArrayParserFormatter(byte[] array) {
        inputStream = new ByteInputStream();
    }

    public byte[] receive(int count) throws IOException {
        byte[] buffer = new byte[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = (byte) inputStream.read();
        }
        return buffer;
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

    public String receiveString() throws IOException {
        int sizeOfBuffer = receiveInt();
        return new String(receive(sizeOfBuffer), StandardCharsets.UTF_8);
    }

    public int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static long fromByteArrayLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public void sendString(String string) throws IOException {
        formatter.sendString(string);
    }

    public void sendBoolean(boolean value) throws IOException {
        formatter.sendBoolean(value);
    }

    public void sendInt(int value) throws IOException {
        formatter.sendInt(value);
    }

    public void sendLong(long value) throws IOException {
        formatter.sendLong(value);
    }

    public void sendRaw(byte[] buffer) throws IOException {
        formatter.sendRaw(buffer);
    }

    public byte[] getBytes() {
        return formatter.getBytes();
    }
    
    

}
