package com.asyncsockets;

/**
 * @author sad
 */
public class Message {

    private byte[] buffer;
    private int messageId;
    private int responseForMessageId;
    private int messageType;

    public Message(byte[] buffer, int messageId, int responseForMessageId, int messageType) {
        this.buffer = buffer;
        this.messageType = messageType;
        this.messageId = messageId;
        this.responseForMessageId = responseForMessageId;
    }

    public int getMessageType() {
        return messageType;
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
