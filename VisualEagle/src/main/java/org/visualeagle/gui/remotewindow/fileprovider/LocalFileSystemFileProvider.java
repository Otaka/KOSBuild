package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.ListenableFutureTask;
import com.asyncsockets.ListenableFutureTaskWithData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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

    @Override
    public ListenableFutureTask<Boolean> renameFile(RFile file, String newName) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(file);
        boolean result = localFileObject.renameTo(new File(localFileObject.getParentFile(), newName));
        future.finishFutureAndReturnData(result);
        return future;
    }

    @Override
    public ListenableFutureTask<Boolean> createFolder(RFile currentFolder, String newFolderName) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(currentFolder);
        File newFolderFileObject = new File(localFileObject, newFolderName);
        boolean result = newFolderFileObject.mkdirs();
        future.finishFutureAndReturnData(result);
        return future;
    }

    @Override
    public ListenableFutureTask<Boolean> exists(RFile file) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(file);
        boolean result = localFileObject.exists();
        future.finishFutureAndReturnData(result);
        return future;
    }

    @Override
    public String separator() {
        return "/";
    }

    private Map<Long, InputStream> filesOpenedForReadingMap = new HashMap<>();
    private Map<Long, OutputStream> filesOpenedForWritingMap = new HashMap<>();
    private AtomicLong handleSequence = new AtomicLong(1);

    @Override
    public ListenableFutureTask<Long> openFileForReading(RFile file) {
        ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(file);
        if (!localFileObject.exists()) {
            future.finishFutureAndReturnException(new FileNotFoundException("File [" + file.getFullPath() + "] does not exists"));
            return future;
        }

        try {
            InputStream stream = new BufferedInputStream(new FileInputStream(localFileObject));
            long handle = handleSequence.incrementAndGet();
            filesOpenedForReadingMap.put(handle, stream);
            future.finishFutureAndReturnData(handle);
        } catch (FileNotFoundException ex) {
            future.finishFutureAndReturnException(new FileNotFoundException("File [" + file.getFullPath() + "] does not exists"));
        }

        return future;
    }

    @Override
    public ListenableFutureTask<Long> openFileForWriting(RFile file, boolean append) {
        ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
        File localFileObject = createFileFromRFile(file);
        try {
            OutputStream stream = new BufferedOutputStream(new FileOutputStream(localFileObject, append));
            long handle = handleSequence.incrementAndGet();
            filesOpenedForWritingMap.put(handle, stream);
            future.finishFutureAndReturnData(handle);
        } catch (FileNotFoundException ex) {
            future.finishFutureAndReturnException(new FileNotFoundException("File [" + file.getFullPath() + "] does not exists"));
        }

        return future;
    }

    @Override
    public ListenableFutureTask<Boolean> close(long handle) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        Closeable toClose = null;
        InputStream rStream = filesOpenedForReadingMap.get(handle);
        if (rStream != null) {
            filesOpenedForReadingMap.remove(handle);
            toClose = rStream;
        }

        OutputStream wStream = filesOpenedForWritingMap.get(handle);
        if (wStream != null) {
            filesOpenedForWritingMap.remove(handle);
            toClose = wStream;
        }

        try {
            toClose.close();
            future.finishFutureAndReturnData(Boolean.TRUE);
        } catch (IOException ex) {
            future.finishFutureAndReturnException(ex);
        }

        return future;
    }

    @Override
    public ListenableFutureTask<Boolean> writeToFile(long handle, byte[] buffer, int count) {
        ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
        OutputStream stream = filesOpenedForWritingMap.get(handle);
        if (stream == null) {
            future.finishFutureAndReturnException(new IllegalArgumentException("Cannot find opened handle to writing file [" + handle + "]"));
            return future;
        }

        if (buffer == null) {
            future.finishFutureAndReturnException(new IllegalArgumentException("Cannot write null buffer to file"));
            return future;
        }

        if (buffer.length == 0) {
            future.finishFutureAndReturnData(Boolean.TRUE);
            return future;
        }

        try {
            for (int i = 0; i < count; i++) {
                stream.write(buffer[i] & 0xFF);
            }

            future.finishFutureAndReturnData(Boolean.TRUE);
        } catch (IOException ex) {
            future.finishFutureAndReturnException(ex);
        }

        return future;
    }

    @Override
    public ListenableFutureTask<Integer> readFromFile(long handle, byte[] buffer) {
        ListenableFutureTaskWithData<Integer> future = new ListenableFutureTaskWithData<>();

        InputStream stream = filesOpenedForReadingMap.get(handle);
        if (stream == null) {
            future.finishFutureAndReturnException(new IllegalArgumentException("Cannot find opened handle to reading file [" + handle + "]"));
            return future;
        }

        if (buffer == null) {
            future.finishFutureAndReturnException(new IllegalArgumentException("Cannot read file to null buffer"));
            return future;
        }

        if (buffer.length == 0) {
            future.finishFutureAndReturnData(0);
            return future;
        }

        try {
            int readBytesCount = IOUtils.read(stream, buffer);
            future.finishFutureAndReturnData(readBytesCount);
        } catch (IOException ex) {
            future.finishFutureAndReturnException(ex);
        }

        return future;
    }
}
