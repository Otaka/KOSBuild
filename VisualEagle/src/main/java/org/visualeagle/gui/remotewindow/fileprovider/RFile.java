package org.visualeagle.gui.remotewindow.fileprovider;

import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sad
 */
public class RFile {

    private String parentPath;

    public String name;
    private long size;
    private boolean directory;
    private long lastModified;
    private AbstractFileProvider fileProvider;

    public RFile(String parentPath, String name, long size, boolean isDirectory, long lastModified, AbstractFileProvider fileProvider) {
        this.parentPath = parentPath;
        if (this.parentPath != null) {
            this.parentPath = this.parentPath.replace('\\', '/').trim();
            this.parentPath = StringUtils.removeEnd(this.parentPath, "/");
        }
        this.name = name;
        this.size = size;
        this.directory = isDirectory;
        this.lastModified = lastModified;
        this.fileProvider = fileProvider;
    }

    public AbstractFileProvider getFileProvider() {
        return fileProvider;
    }

    public String getParentPath() {
        return parentPath;
    }

    public RFile getParentRFile() {
        if (parentPath == null) {
            return null;
        }

        if (parentPath.endsWith(":")) {
            return new RFile(null, parentPath + "/", 0, true, 0, fileProvider);
        } else if (parentPath.endsWith(":/")) {
            return new RFile(null, parentPath, 0, true, 0, fileProvider);
        } else {
            File tempFile = new File(parentPath);
            return new RFile(tempFile.getParent(), tempFile.getName(), 0, true, 0, fileProvider);
        }
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getFullPath() {
        if (parentPath == null) {
            return name;
        } else {
            return parentPath + "/" + name;
        }
    }

    @Override
    public String toString() {
        return getFullPath();
    }

    public String getExtension() {
        int lastIndexOfDot=name.lastIndexOf('.');
        if(lastIndexOfDot==-1){
            return "";
        }

        return name.substring(lastIndexOfDot+1);
    }
}
