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

    public abstract ListenableFutureTask<List<RFile>> listFiles(RFile folder);

    public abstract ListenableFutureTask<List<RFile>> listRoots();

    public abstract ListenableFutureTask<Boolean> removeFile(RFile folder);

    public abstract ListenableFutureTask<Boolean> renameFile(RFile file, String newName);
    public abstract ListenableFutureTask<Boolean> createFolder(RFile currentFolder, String newFolderName);

}
