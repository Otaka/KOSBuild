package org.visualeagle.gui.remotewindow.fileprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sad
 */
public class LocalFileSystemFileProvider extends AbstractFileProvider {

    public LocalFileSystemFileProvider() {
        setCurrentFolder(null);
    }

    @Override
    public List<RFile> listFiles(RFile folder) {
        if (!folder.isDirectory()) {
            throw new RuntimeException("Folder [" + folder.getFullPath() + "] should be folder, but it is a file");
        }

        File localFolderObject = createFileFormRFile(folder);
        List<RFile> files = new ArrayList<>();
        for (File f : localFolderObject.listFiles()) {
            files.add(createRFileFromFile(f));
        }

        return files;
    }

    @Override
    public List<RFile> listRoots() {
        List<RFile> roots = new ArrayList<>();
        for (File root : File.listRoots()) {
            String rootName = root.getPath();
            rootName = StringUtils.removeEnd(root.getPath(), "\\");//fix for windows
            roots.add(new RFile(null, rootName, root.length(), root.isDirectory(), root.lastModified(), this));
        }

        return roots;
    }

    @Override
    public boolean removeFile(RFile file) {
        File localFileObject = createFileFormRFile(file);
        return FileUtils.deleteQuietly(localFileObject);
    }

    private RFile createRFileFromFile(File file) {
        RFile f = new RFile(file.getParent(), file.getName(), file.length(), file.isDirectory(), file.lastModified(), this);
        return f;
    }

    private File createFileFormRFile(RFile file) {
        return new File(file.getParentPath(), file.getName());
    }

}
