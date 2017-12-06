package com;

import com.asyncsockets.AsyncClientSocket;
import com.asyncsockets.AsyncServerSocket;
import com.asyncsockets.ConnectionEvent;
import com.asyncsockets.DataEvent;
import com.asyncsockets.ListenableFutureTaskWithData;
import com.asyncsockets.Request;
import com.asyncsockets.SocketHandler;
import com.asyncsockets.SocketsManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * @author sad
 */
public class Main {

    private static boolean finishFlag;

    public static void main(String[] args) throws UnknownHostException, IOException {
        SocketsManager serverSocketManager = new SocketsManager();
        serverSocketManager.start();

        AsyncServerSocket serverSocket = serverSocketManager.createServerSocket(8090);
        serverSocket.setConnectionEvent(new ConnectionEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {

                try {
                    Request result = (Request) socketHandler.writeWithExpectingResult("0".getBytes(), -1, 2000, null, null).get();
                    while (true) {
                        System.out.println("Server received [" + result.getResponseAsString() + "]");
                        String resultText = result.getResponseAsString();
                        int resultInt = Integer.parseInt(resultText);
                        resultInt++;
                        if (resultInt > 500) {
                            break;
                        }
                        ListenableFutureTaskWithData future = result.writeInResponseWithExpectingResult(String.valueOf(resultInt).getBytes(), 10000, null, null);
                        result = (Request) future.get();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void clientDisconnected(SocketHandler socketHandler) {
                System.out.println("Client disconnected");
            }
        });
        serverSocket.start();
        SocketsManager clientSocketManager = new SocketsManager();
        clientSocketManager.start();
        AsyncClientSocket clientSocket = clientSocketManager.createClientSocket(InetAddress.getLocalHost(), 8090);
        clientSocket.setDataEvent(new DataEvent() {
            @Override
            public void dataArrived(SocketHandler socket, Request request) throws IOException {
                System.out.println("Client first received data " + request.getResponseAsString());
                try {
                    while (true) {
                        System.out.println("Client received [" + request.getResponseAsString() + "]");
                        int receivedInt = Integer.parseInt(request.getResponseAsString());
                        receivedInt++;
                        ListenableFutureTaskWithData future = request.writeInResponseWithExpectingResult(String.valueOf(receivedInt).getBytes(), 10000, null, null);
                        request = (Request) future.get();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sleepUntilFinish(99999);
    }

    private static void sleepUntilFinish(long timeout) {
        long startTime = System.currentTimeMillis();
        while (finishFlag == false) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeout) {
                throw new IllegalStateException("Sleep more then [" + timeout + "]");
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
