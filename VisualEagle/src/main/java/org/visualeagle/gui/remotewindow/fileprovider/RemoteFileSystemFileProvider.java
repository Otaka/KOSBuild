package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import java.util.List;

/**
 * @author sad
 */
public class RemoteFileSystemFileProvider extends AbstractFileProvider{

    @Override
    public String separator() {
        return "/";
    }

    @Override
    public ListenableFutureTask<List<RFile>> listFiles(RFile folder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListenableFutureTask<List<RFile>> listRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
