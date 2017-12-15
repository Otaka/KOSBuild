package com.asyncsockets;

/**
 * @author sad
 */
public class Message {

    private int commandId;
    private byte[] buffer;
    private int messageId;
    private int responseForMessageId;
    private int messageType;

    public Message(int commandId, byte[] buffer, int messageId, int responseForMessageId, int messageType) {
        this.commandId = commandId;
        this.buffer = buffer;
        this.messageType = messageType;
        this.messageId = messageId;
        this.responseForMessageId = responseForMessageId;
    }

    public int getCommandId() {
        return commandId;
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
