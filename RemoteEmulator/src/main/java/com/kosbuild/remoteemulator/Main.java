package com.kosbuild.remoteemulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class Main {

    private Map<Long, InputStream> filesOpenedForReading = new HashMap<>();
    private Map<Long, OutputStream> filesOpenedForWriting = new HashMap<>();
    private static long fileDescriptor = 0;
    private final String ERROR = "0";
    private final String OK = "1";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String connectionTypeString;
        while (true) {
            System.out.println("Create server or connect to client?\n1. Create server\n2. Connect to client");
            connectionTypeString = scanner.nextLine();
            if (connectionTypeString.equalsIgnoreCase("1") || connectionTypeString.equalsIgnoreCase("2")) {
                break;
            }

            System.out.println("Wrong input");
        }

        AbstractSession session;
        if (connectionTypeString.equals("1")) {
            session = createServer(scanner);
        } else if (connectionTypeString.equals("2")) {
            session = createClient(scanner);
        } else {
            throw new IllegalArgumentException("Wrong connection type [" + connectionTypeString + "]");
        }
        new Main().mainLoop(session);
    }

    public void mainLoop(AbstractSession session) throws IOException {
        while (true) {
            String command = session.receiveString();
            if (command.equalsIgnoreCase("LIST_FLDER")) {
                processListFolder(session);
            } else if (command.equalsIgnoreCase("OPENFILE_R")) {
                openFileForReading(session);
            } else if (command.equalsIgnoreCase("OPENFILE_W")) {
                openFileForWriting(session);
            } else if (command.equalsIgnoreCase("WRITE_BUFF")) {
                writeBufferToFile(session);
            } else if (command.equalsIgnoreCase("READ__BUFF")) {
                readBufferFromFile(session);
            } else if (command.equalsIgnoreCase("CLOSE_FILE")) {
                closeFile(session);
            } else if (command.equalsIgnoreCase("DELET_FILE")) {
                deleteFile(session);
            } else if (command.equalsIgnoreCase("COPY__FILE")) {
                copyFile(session);
            } else if (command.equalsIgnoreCase("MOVE__FILE")) {
                moveFile(session);
            } else if (command.equalsIgnoreCase("RUN____APP")) {
                runFile(session);
            }
        }
    }

    private void runFile(AbstractSession session) throws IOException {
        String filePath = session.receiveString();
        System.out.println("Run file [" + filePath + "]");

        session.sendString(OK);
    }

    private void copyFile(AbstractSession session) throws IOException {
        String fileSource = session.receiveString();
        String fileDestination = session.receiveString();
        System.out.println("Copy file [" + fileSource + "] -> [" + fileDestination + "]");
        File sourceFile = new File(fileSource);
        if (!sourceFile.exists()) {
            session.sendString(ERROR);
            session.sendString("Source file does not exists");
            return;
        }
        File destFile = new File(fileDestination);
        if (!destFile.exists()) {
            session.sendString(ERROR);
            session.sendString("Destination file does not exists");
            return;
        }

        if (sourceFile.isDirectory()) {
            FileUtils.copyDirectoryToDirectory(sourceFile, destFile);
        } else {
            FileUtils.copyFileToDirectory(sourceFile, destFile);
        }
        session.sendString(OK);
    }

    private void moveFile(AbstractSession session) throws IOException {
        String fileSource = session.receiveString();
        String fileDestination = session.receiveString();
        System.out.println("Move file [" + fileSource + "] -> [" + fileDestination + "]");
        File sourceFile = new File(fileSource);
        if (!sourceFile.exists()) {
            session.sendString(ERROR);
            session.sendString("Source file does not exists");
            return;
        }
        File destFile = new File(fileDestination);
        if (!destFile.exists()) {
            session.sendString(ERROR);
            session.sendString("Destination file does not exists");
            return;
        }

        if (sourceFile.isDirectory()) {
            FileUtils.moveDirectoryToDirectory(sourceFile, destFile, false);
        } else {
            FileUtils.moveFileToDirectory(sourceFile, destFile, false);
        }
        session.sendString(OK);
    }

    private void deleteFile(AbstractSession session) throws IOException {
        String filePath = session.receiveString();
        System.out.println("Delete file [" + filePath + "]");
        File file = new File(filePath);
        if (!file.exists()) {
            session.sendString(ERROR);
            session.sendString("File does not exists");
            return;
        }

        if (FileUtils.deleteQuietly(file)) {
            session.sendString(OK);
        } else {
            session.sendString(ERROR);
            session.sendString("Cannot remove file " + filePath);
        }

        session.sendString(OK);
    }

    private void closeFile(AbstractSession session) throws IOException {
        long fileDecriptor = session.receiveLong();
        System.out.println("Close file " + fileDecriptor);
        OutputStream stream = filesOpenedForWriting.get(fileDecriptor);
        if (stream != null) {
            filesOpenedForWriting.remove(fileDecriptor);
            session.sendString(OK);
            return;
        }

        InputStream stream2 = filesOpenedForReading.get(fileDecriptor);
        if (stream2 != null) {
            filesOpenedForReading.remove(fileDecriptor);
            session.sendString(OK);
            return;
        }

        session.sendString(ERROR);
        session.sendString("Cannot find descriptor " + fileDecriptor);
    }

    private void writeBufferToFile(AbstractSession session) throws IOException {
        long fileDecriptor = session.receiveLong();
        int bufferSize = session.receiveInt();
        byte[] array = session.receive(bufferSize);
        System.out.println("Write buffer to file [" + fileDecriptor + "] with size " + bufferSize);
        OutputStream stream = filesOpenedForWriting.get(fileDecriptor);
        if (stream == null) {
            session.sendString(ERROR);
            session.sendString("Cannot find descriptor " + fileDecriptor);
            return;
        }

        stream.write(array);
        stream.flush();
        session.sendString(OK);
    }

    private void readBufferFromFile(AbstractSession session) throws IOException {
        long fileDecriptor = session.receiveLong();
        int size = session.receiveInt();
        System.out.println("Read buffer from file [" + fileDecriptor + "] size " + size);
        InputStream stream = filesOpenedForReading.get(fileDecriptor);
        if (stream == null) {
            session.sendString(ERROR);
            session.sendString("Cannot find descriptor " + fileDecriptor);
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
        session.sendString(OK);
        session.sendInt(actualSize);
        session.sendRaw(array);
    }

    private void openFileForReading(AbstractSession session) throws IOException {
        String filePath = session.receiveString();
        System.out.println("Open file for reader [" + filePath + "]");
        File file = new File(filePath);
        if (!file.exists()) {
            session.sendString(ERROR);
            session.sendString("File does not exists");
            return;
        }

        if (file.isDirectory()) {
            session.sendString(ERROR);
            session.sendString("Cannot open directory for reading. Only regular files");
            return;
        }

        InputStream inputStream = IOUtils.buffer(new FileInputStream(file));
        long newDescriptor = fileDescriptor++;
        filesOpenedForReading.put(newDescriptor, inputStream);

        session.sendString(OK);
        session.sendLong(newDescriptor);
    }

    private void openFileForWriting(AbstractSession session) throws IOException {
        String filePath = session.receiveString();
        boolean append = session.receiveBoolean();
        System.out.println("Open file for writing [" + filePath + "] Append " + append);
        File file = new File(filePath);

        if (file.exists() && file.isDirectory()) {
            session.sendString(ERROR);
            session.sendString("Cannot open directory for writing. Only regular files");
            return;
        }

        OutputStream outputStream = IOUtils.buffer(new FileOutputStream(file, append));
        long newDescriptor = fileDescriptor++;
        filesOpenedForWriting.put(newDescriptor, outputStream);
        session.sendString(OK);
        session.sendLong(newDescriptor);
    }

    private void processListFolder(AbstractSession session) throws IOException {
        String folderPath = session.receiveString();
        System.out.println("List folder " + folderPath);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            session.sendString(ERROR);
            session.sendString("Folder does not exists");
            return;
        }

        session.sendString(OK);
        File[] filesArray = folder.listFiles();
        for (File child : filesArray) {
            session.sendString(child.getName());
            session.sendInt(child.isDirectory() == true ? 0 : 1);
            session.sendLong(child.length());
            session.sendLong(child.lastModified());
        }
    }

    private static AbstractSession createServer(Scanner scanner) throws Exception {
        ServerSession serverSession = new ServerSession(8081);
        serverSession.initConnection();
        return serverSession;
    }

    private static AbstractSession createClient(Scanner scanner) throws Exception {
        System.out.println("Please enter hostname to connect");
        String hostname = scanner.nextLine();
        ClientSession clientSession=new ClientSession(hostname);
        clientSession.initConnection();
        return clientSession;
    }
}
