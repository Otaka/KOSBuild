package com.asyncsockets;

import com.asyncsockets.exceptions.AsyncSocketClosed;
import com.asyncsockets.exceptions.AsyncSocketTimeout;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sad
 */
public class SocketHandler {

    private static final int BYTE_DATA_MESSAGE = Integer.MIN_VALUE;
    private static final int PING_DATA_MESSAGE = Integer.MIN_VALUE - 1;
    private boolean belongToServer = false;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] tempIntByteBuffer = new byte[4];
    private List<WaitingFuture> waitingForResponseFutures = new ArrayList<>();
    private Queue<WaitingFuture> waitingFuturesToAdd = new ArrayBlockingQueue<WaitingFuture>(200);
    private Queue<ListenableFutureTask> tasksQueue = new ArrayBlockingQueue<ListenableFutureTask>(100);
    private DataEvent dataArrivedCallback;
    private static AtomicInteger requestIdGenerator = new AtomicInteger(0);
    private static Executor executor = SocketsManager.eventsExecutor;
    private int pingInterval = 2000;
    private long lastOperation = 0;
    private ConnectionEvent connectionEvent;

    public SocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            connectionEvent = null;
            for (WaitingFuture wf : waitingForResponseFutures) {
                wf.getFuture().finishFutureAndReturnException(new AsyncSocketClosed("Socket was closed"));
            }
            for (WaitingFuture wf : waitingFuturesToAdd) {
                wf.getFuture().finishFutureAndReturnException(new AsyncSocketClosed("Socket was closed"));
            }
            waitingForResponseFutures.clear();
            waitingFuturesToAdd.clear();
            tasksQueue.clear();
            dataArrivedCallback = null;

        }
    }

    public void setConnectionEvent(ConnectionEvent connectionEvent) {
        this.connectionEvent = connectionEvent;
    }

    public ConnectionEvent getConnectionEvent() {
        return connectionEvent;
    }

    public void setBelongToServer(boolean belongToServer) {
        this.belongToServer = belongToServer;
    }

    public boolean isBelongToServer() {
        return belongToServer;
    }

    public boolean isClosed() {
        return socket.isClosed();
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
        boolean workDone = false;

        addWaitingFuturesFromQueue();
        checkTimeoutOnWaitingFutures();

        if (processAvailableData()) {
            workDone = true;
        }

        if (processTasks()) {
            workDone = true;
        }

        processPing();
        return workDone;
    }

    private void processPing() throws IOException {
        if (pingInterval > 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastOperation > pingInterval) {
                if (tasksQueue.isEmpty()) {
                    writeMessage(new Message(new byte[0], -1, -1, PING_DATA_MESSAGE));
                }
            }
        }
    }

    private boolean processTasks() {
        if (!tasksQueue.isEmpty()) {
            while (!tasksQueue.isEmpty()) {
                ListenableFutureTask future = tasksQueue.poll();
                if (future != null) {
                    future.run();
                }
            }

            return true;
        }
        return false;
    }

    private boolean processAvailableData() throws IOException {
        int available = inputStream.available();
        if (available > 0) {
            Message message = readMessage();
            if (message.getMessageType() == PING_DATA_MESSAGE) {
                System.out.println("Ping");
            } else {
                if (message.getResponseForMessageId() != -1) {
                    sendMessageToWaitingFuture(message);
                } else {
                    if (dataArrivedCallback != null) {
                        executor.execute(() -> {
                            try {
                                Request response = new Request(message, SocketHandler.this);
                                dataArrivedCallback.dataArrived(SocketHandler.this, response);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });

                    }
                }
            }

            return true;
        }

        return false;
    }

    private long lastCheckTimeoutOnWaitingFuturesTimestamp;

    private void checkTimeoutOnWaitingFutures() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTimeoutOnWaitingFuturesTimestamp > 500) {
            lastCheckTimeoutOnWaitingFuturesTimestamp = currentTime;
            for (int i = 0; i < waitingForResponseFutures.size(); i++) {
                WaitingFuture wf = waitingForResponseFutures.get(i);
                if ((wf.getTimestamp() + wf.getTimeout()) <= currentTime) {
                    sendTimeoutErrorToFuture(wf);
                    waitingForResponseFutures.remove(i);
                    i--;
                }
            }
        }
    }

    private void sendTimeoutErrorToFuture(WaitingFuture waitingFuture) {
        waitingFuture.getFuture().finishFutureAndReturnData(new AsyncSocketTimeout("Timeout for futureid:" + waitingFuture.getResponseId() + ". Waiting more than " + waitingFuture.getTimeout()));
    }

    private void addWaitingFuturesFromQueue() {
        if (!waitingFuturesToAdd.isEmpty()) {
            while (!waitingFuturesToAdd.isEmpty()) {
                waitingForResponseFutures.add(waitingFuturesToAdd.poll());
            }
        }
    }

    public ListenableFutureTask write(byte[] buffer, int responseForRequest, Callback onFinish, Callback onError) {
        if (!isClosed()) {
            ListenableFutureTask future = new ListenableFutureTask((Callable) () -> {
                Message message = new Message(buffer, getNewRequestId(), responseForRequest, BYTE_DATA_MESSAGE);
                writeMessage(message);
                return "OK";
            }, onFinish, onError);
            tasksQueue.offer(future);
            return future;
        } else {
            throw new AsyncSocketClosed("Socket is already closed");
        }
    }

    public ListenableFutureTaskWithData writeWithExpectingResult(byte[] buffer, int responseForRequest, long timeout, Callback onFinish, Callback onError) {
        if (!isClosed()) {
            int newRequestId = getNewRequestId();
            ListenableFutureTaskWithData resultFuture = new ListenableFutureTaskWithData(onFinish, onError);
            WaitingFuture waitingFuture = new WaitingFuture(newRequestId, resultFuture, timeout);
            waitingFuturesToAdd.offer(waitingFuture);
            ListenableFutureTask future = new ListenableFutureTask((Callable) () -> {
                Message message = new Message(buffer, newRequestId, responseForRequest, BYTE_DATA_MESSAGE);
                writeMessage(message);
                return "OK";
            });

            tasksQueue.offer(future);
            return resultFuture;
        } else {
            throw new AsyncSocketClosed("Socket is already closed");
        }
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

        System.out.println("Could not find waiting future with id " + message.getResponseForMessageId());
    }

    private Message readMessage() throws IOException {
        int payloadSize = readInt();
        int messageId = readInt();
        int messageType = readInt();
        int responseForMessageId = readInt();
        byte[] buffer = readBytes(payloadSize);
        Message message = new Message(buffer, messageId, responseForMessageId, messageType);
        lastOperation = System.currentTimeMillis();
        return message;
    }

    private void writeMessage(Message message) throws IOException {
        writeInt(message.getBuffer().length);
        writeInt(message.getMessageId());
        writeInt(message.getMessageType());
        writeInt(message.getResponseForMessageId());
        writeBuffer(message.getBuffer());
        lastOperation = System.currentTimeMillis();
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
