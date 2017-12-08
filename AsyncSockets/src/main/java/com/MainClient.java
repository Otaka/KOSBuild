package com;

import com.asyncsockets.AsyncClientSocket;
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
public class MainClient {

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        SocketsManager clientSocketManager = new SocketsManager();
        clientSocketManager.start();
        AsyncClientSocket clientSocket = clientSocketManager.createClientSocket(InetAddress.getLocalHost(), 8090, new ConnectionEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                System.out.println("Socket connected");
            }

            @Override
            public void clientDisconnected(SocketHandler socketHandler) {
                System.out.println("Socket disconnected");
            }
        }, 2000);

        clientSocket.setDataEvent(new DataEvent() {
            @Override
            public void dataArrived(SocketHandler socket, Request request) throws IOException {
                System.out.println("Client first received data " + request.getResponseAsString());
                try {
                    while (true) {
                        System.out.println("Client received [" + request.getResponseAsString() + "]");
                        int receivedInt = Integer.parseInt(request.getResponseAsString());
                        receivedInt++;
                        if (receivedInt > 500) {
                            break;
                        }

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

        Thread.sleep(999999);
    }
}
