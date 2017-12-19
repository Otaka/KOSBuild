package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import com.asyncsockets.ListenableFutureTaskWithData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * @author sad
 */
public class LocalFileSystemFileProvider extends AbstractFileProvider {

    public LocalFileSystemFileProvider() {
        setCurrentFolder(null);
    }

    private RFile createRFileFromFile(File file) {
        RFile f = new RFile(file.getParent(), file.getName(), file.length(), file.isDirectory(), file.lastModified(), this);
        return f;
    }

    private File createFileFromRFile(RFile file) {
        return new File(file.getParentPath(), file.getName());
    }

    @Override
    public ListenableFutureTask<List<RFile>> listFiles(RFile folder) {
        ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
        if (!folder.isDirectory()) {
            future.finishFutureAndReturnException(new RuntimeException("Folder [" + folder.getFullPath() + "] should be folder, but it is a file"));
        }

        File localFolderObject = createFileFromRFile(folder);
        List<RFile> files = new ArrayList<>();
        File[] children = localFolderObject.listFiles();
        children = (children == null) ? new File[0] : children;
        for (File f : children) {
            files.add(createRFileFromFile(f));
        }

        future.finishFutureAndReturnData(files);
        return future;
    }

    @Override
    public ListenableFutureTask<List<RFile>> listRoots() {
        ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
        List<RFile> roots = new ArrayList<>();
        for (File root : File.listRoots()) {
            String rootName = root.getPath();
            // rootName = StringUtils.removeEnd(root.getPath(), "\\");//fix for windows
            roots.add(new RFile(null, rootName, root.length(), true, root.lastModified(), this));
        }

        future.finishFutureAndReturnData(roots);
        return future;
    }

    @Override
    public ListenableFutureTask<Boolean> removeFile(RFile file) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(file);
        boolean result = FileUtils.deleteQuietly(localFileObject);
        future.finishFutureAndReturnData(result);
        return future;
    }
}
