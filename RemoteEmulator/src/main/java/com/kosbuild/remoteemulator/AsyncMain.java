package com.kosbuild.remoteemulator;

import com.asyncsockets.AsyncClientSocket;
import com.asyncsockets.ConnectionEvent;
import com.asyncsockets.DataEvent;
import com.asyncsockets.Request;
import com.asyncsockets.SocketHandler;
import com.asyncsockets.SocketsManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class AsyncMain {

    private final static int HANDSHAKE_COMMAND = 1;
    private final static int COMMAND_COMMAND = 0;
    private final String ERROR = "5";
    private final String OK = "1";

    private int fileDescriptor = 0;
    private Map<Long, OutputStream> filesOpenedForWriting = new HashMap<>();
    private Map<Long, InputStream> filesOpenedForReading = new HashMap<>();

    private String emulatedRemoteFolder = "d:/temp/remoteFileSystem";

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
                try {
                    System.out.println("Data arrived");
                    processRequest(socket, request);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Thread.sleep(99999999);
    }

    private void processRequest(SocketHandler socket, Request request) throws IOException {
        if (request.getCommand() == HANDSHAKE_COMMAND) {
            String data = request.getResponseAsString();
            String[] parts = data.split("\\|");
            int part1 = Integer.parseInt(parts[0]);
            int part2 = Integer.parseInt(parts[1]);
            request.writeInResponse(HANDSHAKE_COMMAND, ("" + (part1 + part2)).getBytes(StandardCharsets.UTF_8), null, null);
        } else if (request.getCommand() == COMMAND_COMMAND) {
            byte[] result = processCommand(request);
            if (result != null) {
                request.writeInResponse(COMMAND_COMMAND, result);
            }
        } else {
            System.err.println("Unknown command " + request.getCommand());
        }
    }

    public byte[] processCommand(Request session) throws IOException {
        try {
            ByteArrayParserFormatter parser = new ByteArrayParserFormatter(session.getBytes());
            String command = parser.receiveString();
            if (command.equalsIgnoreCase("LIST_ROOTS")) {
                processListRoots(parser);
            } else if (command.equalsIgnoreCase("LIST_FLDER")) {
                processListFolder(parser);
            } else if (command.equalsIgnoreCase("OPENFILE_R")) {
                openFileForReading(parser);
            } else if (command.equalsIgnoreCase("OPENFILE_W")) {
                openFileForWriting(parser);
            } else if (command.equalsIgnoreCase("WRITE_BUFF")) {
                writeBufferToFile(parser);
            } else if (command.equalsIgnoreCase("READ__BUFF")) {
                readBufferFromFile(parser);
            } else if (command.equalsIgnoreCase("CLOSE_FILE")) {
                closeFile(parser);
            } else if (command.equalsIgnoreCase("DELET_FILE")) {
                deleteFile(parser);
            } else if (command.equalsIgnoreCase("MOVE__FILE")) {
                renameFile(parser);
            } else if (command.equalsIgnoreCase("MAKE_FLDER")) {
                createFolder(parser);
            } else if (command.equalsIgnoreCase("CHCK_EXIST")) {
                checkFileExists(parser);
            } else if (command.equalsIgnoreCase("RUN____APP")) {
                runFile(parser);
            } else {
                parser.sendString(ERROR);
                parser.sendString("RemoteEmulator:[Unknown command '" + command + "']]");
                return parser.getBytes();
            }
            return parser.getBytes();
        } catch (Exception ex) {
            ex.printStackTrace();
            ByteArrayParserFormatter bp = new ByteArrayParserFormatter(new byte[0]);
            bp.sendString(ERROR);
            bp.sendString(ex.getMessage());
            return bp.getBytes();
        }
    }

    private void runFile(ByteArrayParserFormatter parser) throws IOException {
        String filePath = parser.receiveString();
        System.out.println("Run file [" + filePath + "]");

        parser.sendString(OK);
    }

    private void renameFile(ByteArrayParserFormatter parser) throws IOException {
        String fileSource = parser.receiveString();
        String fileDestination = parser.receiveString();
        System.out.println("Rename file [" + fileSource + "] -> [" + fileDestination + "]");
        File sourceFile = getFile(fileSource);
        if (!sourceFile.exists()) {
            parser.sendString(ERROR);
            parser.sendString("Source file does not exists");
            return;
        }
        File destFile = getFile(fileDestination);
        if (destFile.exists()) {
            parser.sendString(ERROR);
            parser.sendString("Destination file already exists");
            return;
        }
        System.out.println("Rename file [" + sourceFile.getAbsolutePath() + "] to [" + destFile.getAbsolutePath() + "]");
        if (sourceFile.renameTo(destFile)) {
            parser.sendString(OK);
        } else {
            parser.sendString(ERROR);
            parser.sendString("Cannot rename file");
        }
    }

    private void createFolder(ByteArrayParserFormatter parser) throws IOException {
        String folder = parser.receiveString();
        String newFolderName = parser.receiveString();
        System.out.println("Make folder [" + newFolderName + "] in [" + folder + "]");
        File sourceFolder = getFile(folder);
        if (!sourceFolder.exists()) {
            parser.sendString(ERROR);
            parser.sendString("Folder [" + folder + "] does not exists");
            return;
        }

        File newFolderFileObject = new File(sourceFolder, newFolderName);
        if (newFolderFileObject.exists()) {
            parser.sendString(ERROR);
            parser.sendString("Folder [" + newFolderName + "] already exists");
            return;
        }

        if (newFolderFileObject.mkdirs()) {
            parser.sendString(OK);
        } else {
            parser.sendString(ERROR);
            parser.sendString("Cannot create folder");
        }
    }

    private void checkFileExists(ByteArrayParserFormatter parser) throws IOException {
        String filePath = parser.receiveString();
        System.out.println("Check file exists [" + filePath + "]");
        File file = getFile(filePath);
        parser.sendString(OK);
        parser.sendBoolean(file.exists());
    }

    private void deleteFile(ByteArrayParserFormatter parser) throws IOException {
        String filePath = parser.receiveString();
        System.out.println("Delete file [" + filePath + "]");
        File file = getFile(filePath);
        if (!file.exists()) {
            parser.sendString(ERROR);
            parser.sendString("File does not exists");
            return;
        }

        if (FileUtils.deleteQuietly(file)) {
            parser.sendString(OK);
        } else {
            parser.sendString(ERROR);
            parser.sendString("Cannot remove file " + filePath);
        }
    }

    private void closeFile(ByteArrayParserFormatter parser) throws IOException {
        long fileDecriptor = parser.receiveLong();
        System.out.println("Close file " + fileDecriptor);
        OutputStream stream = filesOpenedForWriting.get(fileDecriptor);
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ex) {
                parser.sendString(ERROR);
                parser.sendString("Error while try to close file [" + fileDecriptor + "] opened for writing");
                return;
            }
            filesOpenedForWriting.remove(fileDecriptor);
            parser.sendString(OK);
            return;
        }

        InputStream stream2 = filesOpenedForReading.get(fileDecriptor);
        if (stream2 != null) {
            try {
                stream2.close();
            } catch (Exception ex) {
                parser.sendString(ERROR);
                parser.sendString("Error while try to close file [" + fileDecriptor + "] opened for reading");
                return;
            }

            filesOpenedForReading.remove(fileDecriptor);
            parser.sendString(OK);
            return;
        }

        parser.sendString(ERROR);
        parser.sendString("Cannot find descriptor " + fileDecriptor);
    }

    private void writeBufferToFile(ByteArrayParserFormatter parser) throws IOException {
        long fileDecriptor = parser.receiveLong();
        int bufferSize = parser.receiveInt();
        byte[] array = parser.receive(bufferSize);
        System.out.println("Write buffer to file [" + fileDecriptor + "] with size " + bufferSize);
        OutputStream stream = filesOpenedForWriting.get(fileDecriptor);
        if (stream == null) {
            parser.sendString(ERROR);
            parser.sendString("Cannot find descriptor " + fileDecriptor);
            return;
        }

        stream.write(array);
        stream.flush();
        parser.sendString(OK);
    }

    private void readBufferFromFile(ByteArrayParserFormatter parser) throws IOException {
        long fileDecriptor = parser.receiveLong();
        int size = parser.receiveInt();
        System.out.println("Read buffer from file [" + fileDecriptor + "] size " + size);
        InputStream stream = filesOpenedForReading.get(fileDecriptor);
        if (stream == null) {
            parser.sendString(ERROR);
            parser.sendString("Cannot find descriptor " + fileDecriptor);
            return;
        }

        byte[] array = new byte[size];
        int actualSize = 0;
        for (int i = 0; i < size; i++, actualSize++) {
            int value = stream.read();
            if (value == -1) {
                break;
            }

            array[i] = (byte) value;
        }

        array = Arrays.copyOf(array, actualSize);
        parser.sendString(OK);
        parser.sendInt(actualSize);
        parser.sendRaw(array);
    }

    private void openFileForReading(ByteArrayParserFormatter parser) throws IOException {
        String filePath = parser.receiveString();
        System.out.println("Open file for reader [" + filePath + "]");
        File file = getFile(filePath);
        if (!file.exists()) {
            parser.sendString(ERROR);
            parser.sendString("File does not exists");
            return;
        }

        if (file.isDirectory()) {
            parser.sendString(ERROR);
            parser.sendString("Cannot open directory for reading. Only regular files");
            return;
        }

        InputStream inputStream = IOUtils.buffer(new FileInputStream(file));
        long newDescriptor = fileDescriptor++;
        filesOpenedForReading.put(newDescriptor, inputStream);

        parser.sendString(OK);
        parser.sendLong(newDescriptor);
    }

    private void openFileForWriting(ByteArrayParserFormatter parser) throws IOException {
        String filePath = parser.receiveString();
        boolean append = parser.receiveBoolean();
        System.out.println("Open file for writing [" + filePath + "] Append " + append);
        File file = getFile(filePath);

        if (file.exists() && file.isDirectory()) {
            parser.sendString(ERROR);
            parser.sendString("Cannot open directory for writing. Only regular files");
            return;
        }

        OutputStream outputStream = IOUtils.buffer(new FileOutputStream(file, append));
        long newDescriptor = fileDescriptor++;
        filesOpenedForWriting.put(newDescriptor, outputStream);
        parser.sendString(OK);
        parser.sendLong(newDescriptor);
    }

    private void processListFolder(ByteArrayParserFormatter parser) throws IOException {
        String folderPath = parser.receiveString();

        System.out.println("List folder " + folderPath);
        File folder = getFile(folderPath);
        System.out.println("Emulated folder " + folder.getAbsolutePath());
        if (!folder.exists()) {
            parser.sendString(ERROR);
            parser.sendString("Folder does not exists");
            return;
        }

        parser.sendString(OK);
        File[] filesArray = folder.listFiles();
        parser.sendInt(filesArray.length);
        for (File child : filesArray) {
            parser.sendString(child.getName());
            parser.sendBoolean(child.isDirectory());
            parser.sendLong(child.length());
            parser.sendLong(child.lastModified());
        }
    }

    private void processListRoots(ByteArrayParserFormatter parser) throws IOException {
        System.out.println("List roots");
        parser.sendString(OK);
        parser.sendInt(1);//count of strings
        parser.sendString("/");//actual root folder
    }

    private String concatFileParts(String p1, String p2) {
        if (!p1.endsWith("/") && !p2.startsWith("/")) {
            return p1 + "/" + p2;
        }
        if (p1.endsWith("/") && p2.startsWith("/")) {
            return p1 + p2.substring(1);
        }
        return p1 + p2;
    }
    
    private File getFile(String path) {
        return new File(concatFileParts(emulatedRemoteFolder, path));
    }
}
