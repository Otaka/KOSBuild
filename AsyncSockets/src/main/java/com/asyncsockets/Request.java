package com.asyncsockets;

/**
 * @author sad
 */
public class Request {

    private Message message;
    private SocketHandler socketHandler;

    public Request(Message message, SocketHandler socketHandler) {
        this.message = message;
        this.socketHandler = socketHandler;
    }

    public int getCommand() {
        return message.getCommandId();
    }

    public byte[] getBytes() {
        return message.getBuffer();
    }

    public ListenableFutureTask writeInResponse(int commandId, byte[] buffer, Callback onFinish, Callback onError) {
        return socketHandler.write(commandId, buffer, message.getMessageId(), onFinish, onError);
    }
    
    public ListenableFutureTask writeInResponse(int commandId, byte[] buffer) {
        return socketHandler.write(commandId, buffer, message.getMessageId(), null, null);
    }

    public ListenableFutureTaskWithData writeInResponseWithExpectingResult(int commandId, byte[] buffer, long timeout, Callback onFinish, Callback onError) {
        return socketHandler.writeWithExpectingResult(commandId, buffer, message.getMessageId(), timeout, onFinish, onError);
    }

    public String getResponseAsString() {
        return new String(message.getBuffer());
    }
}
