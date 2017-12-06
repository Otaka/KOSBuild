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

    public byte[] getBytes() {
        return message.getBuffer();
    }

    public ListenableFutureTask writeInResponse(byte[] buffer, Callback onFinish, Callback onError) {
        return socketHandler.write(buffer,message.getMessageId(), onFinish, onError);
    }

    public ListenableFutureTaskWithData writeInResponseWithExpectingResult(byte[] buffer, long timeout, Callback onFinish, Callback onError) {
        return socketHandler.writeWithExpectingResult(buffer,message.getMessageId(),timeout, onFinish, onError);
    }
    
    public String getResponseAsString(){
        return new String(message.getBuffer());
    }
}
