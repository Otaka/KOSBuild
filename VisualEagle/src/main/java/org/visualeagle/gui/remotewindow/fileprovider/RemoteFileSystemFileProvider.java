package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import com.asyncsockets.ListenableFutureTaskWithData;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.visualeagle.gui.remoteconnection.RemoteConnectionManager;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class RemoteFileSystemFileProvider extends AbstractFileProvider {

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<List<RFile>> listRoots() {
        ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
        List<RFile> roots = new ArrayList<>();

        RemoteConnectionManager connectionManager = getConnectionManager(future);
        connectionManager.getCurrentConnection().writeWithExpectingResult(0, "LIST_ROOTS".getBytes(StandardCharsets.UTF_8), 10_000, (a) -> {
            System.out.println("A="+a);
        }, (a) -> {
            System.out.println("Error A="+a);
        });
        future.finishFutureAndReturnData(roots);
        return future;
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
