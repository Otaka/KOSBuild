package com.asockets;

import com.asyncsockets.SocketsManager;
import com.asyncsockets.AsyncServerSocket;
import com.asyncsockets.ListenableFutureTaskWithData;
import com.asyncsockets.DataEvent;
import com.asyncsockets.AsyncClientSocket;
import com.asyncsockets.ConnectionEvent;
import com.asyncsockets.Request;
import com.asyncsockets.SocketHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author sad
 */
public class SocketsManagerTest {

    private static boolean finishFlag;

    public SocketsManagerTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testStart() throws UnknownHostException, IOException, InterruptedException {
        SocketsManager serverSocketManager = new SocketsManager();
        serverSocketManager.start();

        AsyncServerSocket serverSocket = serverSocketManager.createServerSocket(8090);
        serverSocket.setConnectionEvent(new ConnectionEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                System.out.println("Client connected [" + socketHandler.getRemoteAddress() + "]");
                try {
                    Request result = (Request) socketHandler.writeWithExpectingResult("1 hello client".getBytes(), -1, 2000, null, null).get();
                    String text = "3[" + new String(result.getBytes()) + "]";
                    System.out.println(text);
                    result.writeInResponse(text.getBytes(), null, null);
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
                String val = new String(request.getBytes());
                String text = "2[" + val + "]";
                System.out.println(text);
                ListenableFutureTaskWithData future = request.writeInResponseWithExpectingResult(text.getBytes(), 2000, null, null);
                Request result;
                try {
                    result = (Request) future.get();
                    text = "4[" + new String(result.getBytes()) + "]";
                    System.out.println(text);
                    finishFlag=true;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sleepUntilFinish(9999);
    }

    private void sleepUntilFinish(long timeout) {
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
