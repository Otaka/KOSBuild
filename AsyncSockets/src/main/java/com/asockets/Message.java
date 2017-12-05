package com.asockets;

/**
 * @author sad
 */
public class Message {

    private byte[] buffer;
    private int messageId;
    private int responseForMessageId;

    public Message(byte[] buffer, int messageId, int responseForMessageId) {
        this.buffer = buffer;
        this.messageId = messageId;
        this.responseForMessageId = responseForMessageId;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getResponseForMessageId() {
        return responseForMessageId;
    }

}
