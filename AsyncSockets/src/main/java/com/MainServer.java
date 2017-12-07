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
public class MainServer {

    private static boolean finishFlag;

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
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

        Thread.sleep(999999);
    }

}
