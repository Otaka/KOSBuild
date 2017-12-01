package org.visualeagle.gui.remotewindow.fileprovider;

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

    public abstract List<RFile> listFiles(RFile folder);

    public abstract List<RFile> listRoots();

    public abstract boolean removeFile(RFile folder);

}
