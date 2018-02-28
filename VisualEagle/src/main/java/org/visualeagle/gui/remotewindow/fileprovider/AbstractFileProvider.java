package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import java.util.List;

/**
 * @author sad
 */
public abstract class AbstractFileProvider {

    private RFile currentFolder;

    public RFile getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(RFile currentFolder) {
        this.currentFolder = currentFolder;
    }
    public abstract String separator();
    public abstract ListenableFutureTask<List<RFile>> listFiles(RFile folder);

    public abstract ListenableFutureTask<List<RFile>> listRoots();

    public abstract ListenableFutureTask<Boolean> removeFile(RFile folder);

    public abstract ListenableFutureTask<Boolean> renameFile(RFile file, String newName);

    public abstract ListenableFutureTask<Boolean> createFolder(RFile currentFolder, String newFolderName);

    public abstract ListenableFutureTask<Boolean> exists(RFile file);
    
    /**
    Return handle to file
    */
    public abstract ListenableFutureTask<Long> openFileForReading(RFile file);

    /**
    Return handle to file
    */
    public abstract ListenableFutureTask<Long> openFileForWriting(RFile file, boolean append);
    
    public abstract ListenableFutureTask<Boolean> writeToFile(long handle, byte[]buffer,int count);
    
    /**
    Tries to fully fill buffer from file opened with @handle<br>
    returns number of actually read bytes
    */
    public abstract ListenableFutureTask<Integer> readFromFile(long handle, byte[]buffer);
    
    public abstract ListenableFutureTask<Boolean> close(long handle);
    
}
