package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import com.asyncsockets.ListenableFutureTaskWithData;
import com.asyncsockets.Request;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.visualeagle.gui.remoteconnection.RemoteConnectionManager;
import org.visualeagle.utils.ByteArrayFormatter;
import org.visualeagle.utils.ByteArrayParserFormatter;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Utils;

/**
 * @author sad
 */
public class RemoteFileSystemFileProvider extends AbstractFileProvider {

    private int defaultTimeout = 100_000;
    private String ERROR = "5";
    private String OK = "1";

    @Override
    public String separator() {
        return "/";
    }

    private RemoteConnectionManager getConnectionManager(ListenableFutureTaskWithData currentFutureUsedForReportError) {
        RemoteConnectionManager connectionManager = Lookup.get().get(RemoteConnectionManager.class);
        if (!connectionManager.isConnectionEstablished()) {
            currentFutureUsedForReportError.finishFutureAndReturnException(new IllegalStateException("Connection is not established"));
        }
        return connectionManager;
    }

    @Override
    public ListenableFutureTask<List<RFile>> listFiles(RFile folder) {
        try {
            ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("LIST_FLDER");
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, (requestObject) -> {
                try {
                    ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                    String ok = parser.receiveString();
                    if (ok.equals(ERROR)) {
                        String errorMessage = parser.receiveString();
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                        System.out.println("Client send a message [" + errorMessage + "]");
                        return;
                    } else if (!ok.equals(OK)) {
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                        return;
                    }

                    List<RFile> children = new ArrayList<>();
                    int count = parser.receiveInt();
                    for (int i = 0; i < count; i++) {
                        String name = parser.receiveString();
                        boolean isDirectory=parser.receiveBoolean();
                        long size=parser.receiveLong();
                        long lastModified=parser.receiveLong();
                        RFile childFile = new RFile(folder.getFullPath(), name,size, isDirectory, lastModified, this);
                        children.add(childFile);
                    }

                    future.finishFutureAndReturnData(children);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    future.finishFutureAndReturnException(ex);
                }
            }, (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to get list of root folders in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<List<RFile>> listRoots() {
        try {
            ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("LIST_ROOTS");
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, (requestObject) -> {
                try {
                    ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                    String ok = parser.receiveString();
                    if (ok.equals(ERROR)) {
                        String errorMessage = parser.receiveString();
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                        System.out.println("Client send a message [" + errorMessage + "]");
                        return;
                    } else if (!ok.equals(OK)) {
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                        return;
                    }

                    List<RFile> roots = new ArrayList<>();
                    int count = parser.receiveInt();
                    for (int i = 0; i < count; i++) {
                        String path = parser.receiveString();
                        RFile rootFile = new RFile(null, path, 0, true, 0, this);
                        roots.add(rootFile);
                    }

                    future.finishFutureAndReturnData(roots);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    future.finishFutureAndReturnException(ex);
                }
            }, (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to get list of root folders in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> removeFile(List<RFile> folder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Boolean> renameFile(RFile file, String newName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Boolean> createFolder(RFile currentFolder, String newFolderName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Boolean> exists(RFile file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Integer> openFileForReading(RFile file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Integer> openFileForWriting(RFile file, boolean append) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Boolean> writeToFile(int handle, byte[] buffer, int count) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Integer> readFromFile(int handle, byte[] buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<Boolean> close(int handle) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
