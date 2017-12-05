package com.asockets;


/**
 * @author sad
 */
public class Request {

    private Message message;
    private ASocketHandler socketHandler;

    public Request(Message message, ASocketHandler socketHandler) {
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
}
