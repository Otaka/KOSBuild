package org.visualeagle.gui.remotewindow.fileprovider;

import java.util.List;

/**
 * @author sad
 */
public abstract class AbstractFileProvider {

    public abstract List<RFile> listFiles(RFile folder);

    public abstract List<RFile> listRoots();
    public abstract boolean removeFile(RFile folder);

}
