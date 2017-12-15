package com.kosbuild.remoteemulator;

import com.asyncsockets.AsyncClientSocket;
import com.asyncsockets.ConnectionEvent;
import com.asyncsockets.DataEvent;
import com.asyncsockets.Request;
import com.asyncsockets.SocketHandler;
import com.asyncsockets.SocketsManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author sad
 */
public class AsyncMain {

    private final static int HANDSHAKE_COMMAND = 1;

    public static void main(String[] args) throws IOException, UnknownHostException, InterruptedException {
        new AsyncMain().asyncMainAppStart(args);

    }

    public void asyncMainAppStart(String[] args) throws UnknownHostException, IOException, InterruptedException {
        System.out.println("Async client started");
        SocketsManager socketsManager = new SocketsManager();
        socketsManager.start();

        String hostPortLine;
        if (args.length > 0) {
            hostPortLine = args[0];
        } else {
            System.out.println("Please enter hostname to connect");
            hostPortLine = new Scanner(System.in).nextLine();
        }

        if (!hostPortLine.contains(":")) {
            System.err.println("Server address should be in HOST:PORT format, but found [" + hostPortLine + "]");
            System.exit(1);
        }

        String host = hostPortLine.substring(0, hostPortLine.indexOf(':'));
        int port = Integer.parseInt(hostPortLine.substring(hostPortLine.indexOf(':') + 1));
        AsyncClientSocket socket = socketsManager.createClientSocket(InetAddress.getByName(host), port, new ConnectionEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                System.out.println("Client connected");
            }

            @Override
            public void clientDisconnected(SocketHandler socketHandler) {
                System.out.println("Client disconnected");
            }
        }, 4000, new DataEvent() {
            @Override
            public void dataArrived(SocketHandler socket, Request request) throws IOException {
                System.out.println("Data arrived");
                processRequest(request);
            }
        });

        Thread.sleep(99999999);
    }

    private void processRequest(Request request) {
        if (request.getCommand() == HANDSHAKE_COMMAND) {
            String data = request.getResponseAsString();
            String[] parts = data.split("\\|");
            int part1 = Integer.parseInt(parts[0]);
            int part2 = Integer.parseInt(parts[1]);
            request.writeInResponse(HANDSHAKE_COMMAND, ("" + (part1 + part2)).getBytes(StandardCharsets.UTF_8), null, null);
        }else{
            System.err.println("Unknown command "+request.getCommand());
        }
    }
}
