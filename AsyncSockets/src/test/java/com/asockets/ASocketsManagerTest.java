package com.asockets;

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
public class ASocketsManagerTest {

    private static boolean finishFlag;

    public ASocketsManagerTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testStart() throws UnknownHostException, IOException, InterruptedException {
        ASocketsManager serverSocketManager = new ASocketsManager();
        serverSocketManager.start();

        AServerSocket serverSocket = serverSocketManager.createServerSocket(8090);
        serverSocket.setConnectionEvent(new ConnectionEvent() {
            @Override
            public void clientConnected(ASocketHandler socketHandler) {
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
            public void clientDisconnected(ASocketHandler socketHandler) {
                System.out.println("Client disconnected");
            }
        });
        serverSocket.start();
        ASocketsManager clientSocketManager = new ASocketsManager();
        clientSocketManager.start();
        AClientSocket clientSocket = clientSocketManager.createClientSocket(InetAddress.getLocalHost(), 8090);
        clientSocket.setDataEvent(new DataEvent() {
            @Override
            public void dataArrived(ASocketHandler socket, Request request) throws IOException {
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
