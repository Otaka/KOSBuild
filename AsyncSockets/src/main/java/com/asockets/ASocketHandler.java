package com.asockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sad
 */
public class ASocketHandler {

    private boolean belongToServer = false;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] tempIntByteBuffer = new byte[4];
    private List<WaitingFuture> waitingForResponseFutures = new ArrayList<>();
    private Queue<ListenableFutureTask> tasksQueue = new ArrayBlockingQueue<ListenableFutureTask>(100);
    private DataEvent dataArrivedCallback;
    private static AtomicInteger requestIdGenerator = new AtomicInteger(0);
    private Executor executor = Executors.newCachedThreadPool();

    public ASocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void setBelongToServer(boolean belongToServer) {
        this.belongToServer = belongToServer;
    }

    public boolean isBelongToServer() {
        return belongToServer;
    }

    public SocketAddress getRemoteAddress() {
        return socket.getRemoteSocketAddress();
    }

    public static int getNewRequestId() {
        return requestIdGenerator.incrementAndGet();
    }

    public void setDataArrivedCallback(DataEvent dataArrivedCallback) {
        this.dataArrivedCallback = dataArrivedCallback;
    }

    public boolean process() throws IOException {
        int available = inputStream.available();
        if (available > 0) {
            Message message = readMessage();
            if (message.getResponseForMessageId() != -1) {
                sendMessageToWaitingFuture(message);
            } else {
                if (dataArrivedCallback != null) {
                    executor.execute(() -> {
                        try {
                            Request response = new Request(message, ASocketHandler.this);
                            dataArrivedCallback.dataArrived(ASocketHandler.this, response);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });

                }
            }
        }
        while (!tasksQueue.isEmpty()) {
            ListenableFutureTask future = tasksQueue.poll();
            if (future != null) {
                future.run();
            }
        }
        return true;
    }

    public ListenableFutureTask write(byte[] buffer, int responseForRequest, Callback onFinish, Callback onError) {
        ListenableFutureTask future = new ListenableFutureTask((Callable) () -> {
            Message message = new Message(buffer, getNewRequestId(), responseForRequest);
            writeMessage(message);
            return "OK";
        }, onFinish, onError);
        tasksQueue.offer(future);
        return future;
    }

    public ListenableFutureTaskWithData writeWithExpectingResult(byte[] buffer, int responseForRequest, long timeout, Callback onFinish, Callback onError) {
        int newRequestId = getNewRequestId();
        ListenableFutureTaskWithData resultFuture = new ListenableFutureTaskWithData(onFinish, onError);
        WaitingFuture waitingFuture = new WaitingFuture(newRequestId, resultFuture, timeout);
        waitingForResponseFutures.add(waitingFuture);
        ListenableFutureTask future = new ListenableFutureTask((Callable) () -> {
            Message message = new Message(buffer, newRequestId, responseForRequest);
            writeMessage(message);
            return "OK";
        });

        tasksQueue.offer(future);
        return resultFuture;
    }

    private void sendMessageToWaitingFuture(Message message) {
        for (int i = 0; i < waitingForResponseFutures.size(); i++) {
            WaitingFuture wf = waitingForResponseFutures.get(i);
            if (wf.getResponseId() == message.getResponseForMessageId()) {
                Request response = new Request(message, this);
                wf.getFuture().finishFutureAndReturnData(response);
                waitingForResponseFutures.remove(i);
                return;
            }
        }
    }

    private Message readMessage() throws IOException {
        int payloadSize = readInt();
        int messageId = readInt();
        int responseForMessageId = readInt();
        byte[] buffer = readBytes(payloadSize);
        Message message = new Message(buffer, messageId, responseForMessageId);
        return message;
    }

    private void writeMessage(Message message) throws IOException {
        writeInt(message.getBuffer().length);
        writeInt(message.getMessageId());
        writeInt(message.getResponseForMessageId());
        writeBuffer(message.getBuffer());
    }

    private int readInt() throws IOException {
        readBytes(tempIntByteBuffer);
        return fromByteArray(tempIntByteBuffer);
    }

    private void writeBuffer(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    private void writeInt(int value) throws IOException {
        intToByteArray(value, tempIntByteBuffer);
        outputStream.write(tempIntByteBuffer);
    }

    private byte[] readBytes(int size) throws IOException {
        byte[] buffer = new byte[size];
        readBytes(buffer);
        return buffer;
    }

    private void readBytes(byte[] buffer) throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            int value = inputStream.read();
            buffer[i] = (byte) value;
        }
    }

    private static void intToByteArray(int value, byte[] buffer) {
        buffer[0] = (byte) (value >>> 24);
        buffer[1] = (byte) (value >>> 16);
        buffer[2] = (byte) (value >>> 8);
        buffer[3] = (byte) value;
    }

    private static int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
