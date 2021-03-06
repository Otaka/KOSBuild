package org.visualeagle.gui.remotewindow.fileprovider;

import com.asyncsockets.Callback;
import com.asyncsockets.ListenableFutureTask;
import com.asyncsockets.ListenableFutureTaskWithData;
import com.asyncsockets.Request;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import org.visualeagle.gui.remoteconnection.RemoteConnectionManager;
import org.visualeagle.utils.ByteArrayFormatter;
import org.visualeagle.utils.ByteArrayParserFormatter;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Utils;

/**
 * @author sad
 */
public class RemoteFileSystemFileProvider extends AbstractFileProvider {

    private int defaultTimeout = 100_000;
    private String ERROR = "5";
    private String OK = "1";

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
        try {
            ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("LIST_FLDER");
            byteFormatter.sendString(folder.getFullPath());
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, (requestObject) -> {
                try {
                    ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                    String ok = parser.receiveString();
                    if (ok.equals(ERROR)) {
                        String errorMessage = parser.receiveString();
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                        System.out.println("Client send a message [" + errorMessage + "]");
                        return;
                    } else if (!ok.equals(OK)) {
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                        return;
                    }

                    List<RFile> children = new ArrayList<>();
                    int count = parser.receiveInt();
                    for (int i = 0; i < count; i++) {
                        String name = parser.receiveString();
                        boolean isDirectory = parser.receiveBoolean();
                        long size = parser.receiveLong();
                        long lastModified = parser.receiveLong();
                        RFile childFile = new RFile(folder.getFullPath(), name, size, isDirectory, lastModified, this);
                        children.add(childFile);
                    }

                    future.finishFutureAndReturnData(children);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    future.finishFutureAndReturnException(ex);
                }
            }, (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to get list folders in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<List<RFile>> listRoots() {
        try {
            ListenableFutureTaskWithData<List<RFile>> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("LIST_ROOTS");
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, (requestObject) -> {
                try {
                    ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                    String ok = parser.receiveString();
                    if (ok.equals(ERROR)) {
                        String errorMessage = parser.receiveString();
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                        System.out.println("Client send a message [" + errorMessage + "]");
                        return;
                    } else if (!ok.equals(OK)) {
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                        return;
                    }

                    List<RFile> roots = new ArrayList<>();
                    int count = parser.receiveInt();
                    for (int i = 0; i < count; i++) {
                        String path = parser.receiveString();
                        RFile rootFile = new RFile(null, path, 0, true, 0, this);
                        roots.add(rootFile);
                    }

                    future.finishFutureAndReturnData(roots);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    future.finishFutureAndReturnException(ex);
                }
            }, (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to get list root folders in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> removeFile(RFile folder) {
        try {
            ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("DELET_FILE");
            byteFormatter.sendString(folder.getFullPath());
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, null), (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to remove file in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private String concatFileParts(String p1, String p2) {
        if (!p1.endsWith("/") && !p2.startsWith("/")) {
            return p1 + "/" + p2;
        }
        if (p1.endsWith("/") && p2.startsWith("/")) {
            return p1 + p2.substring(1);
        }
        return p1 + p2;
    }

    @Override
    public ListenableFutureTask<Boolean> renameFile(RFile file, String newName) {
        try {
            ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("MOVE__FILE");
            byteFormatter.sendString(file.getFullPath());
            String newFileName = concatFileParts((file.getParentPath().equals("") ? "/" : file.getParentPath()), newName);
            byteFormatter.sendString(newFileName);
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, null), (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to rename file in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> createFolder(RFile currentFolder, String newFolderName) {
        try {
            ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("MAKE_FLDER");
            byteFormatter.sendString(currentFolder.getFullPath());
            byteFormatter.sendString(newFolderName);
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, null), (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to create folder [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> exists(RFile file) {
        try {
            ListenableFutureTaskWithData<Boolean> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("CHCK_EXIST");
            byteFormatter.sendString(file.getFullPath());
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, (requestObject) -> {
                try {
                    ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                    String ok = parser.receiveString();
                    if (ok.equals(ERROR)) {
                        String errorMessage = parser.receiveString();
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                        System.out.println("Client send a message [" + errorMessage + "]");
                        return;
                    } else if (!ok.equals(OK)) {
                        future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                        return;
                    }
                    boolean result = parser.receiveBoolean();
                    future.finishFutureAndReturnData(result);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    future.finishFutureAndReturnException(ex);
                }
            }, (a) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to get list of root folders in remote file system [" + a + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Long> openFileForReading(RFile file) {
        try {
            ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("OPENFILE_R");
            byteFormatter.sendString(file.getFullPath());
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, (fut, byteArrayParser) -> {
                long newDescriptor = byteArrayParser.receiveLong();
                fut.finishFutureAndReturnData(newDescriptor);
            }), (exception) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to open file for reading [" + exception + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Long> openFileForWriting(RFile file, boolean append) {
        try {
            ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("OPENFILE_W");
            byteFormatter.sendString(file.getFullPath());
            byteFormatter.sendBoolean(append);
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, (fut, byteArrayParser) -> {
                long newDescriptor = byteArrayParser.receiveLong();
                fut.finishFutureAndReturnData(newDescriptor);
            }), (exception) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to open file for writing [" + exception + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> close(long handle) {
        try {
            ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("CLOSE_FILE");
            byteFormatter.sendLong(handle);
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, null), (exception) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to close file [" + exception + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Boolean> writeToFile(long handle, byte[] buffer, int count) {
        try {
            ListenableFutureTaskWithData<Long> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("WRITE_BUFF");
            byteFormatter.sendLong(handle);
            byteFormatter.sendInt(count);
            if (buffer.length == count) {
                byteFormatter.sendRaw(buffer);
            } else {
                byte[] newBuffer = Arrays.copyOf(buffer, count);
                byteFormatter.sendRaw(newBuffer);
            }
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, null), (exception) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to write buffer to file [" + exception + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ListenableFutureTask<Integer> readFromFile(long handle, byte[] buffer) {
        try {
            ListenableFutureTaskWithData<Integer> future = new ListenableFutureTaskWithData<>();
            RemoteConnectionManager connectionManager = getConnectionManager(future);
            ByteArrayFormatter byteFormatter = new ByteArrayFormatter();
            byteFormatter.sendString("READ__BUFF");
            byteFormatter.sendLong(handle);
            byteFormatter.sendInt(buffer.length);
            connectionManager.getCurrentConnection().writeWithExpectingResult(0, byteFormatter.getBytes(), defaultTimeout, createResultCallback(future, (fut, byteArrayParser) -> {
                int actualSize = byteArrayParser.receiveInt();
                byte[] newBuffer = byteArrayParser.receive(actualSize);
                for (int i = 0; i < actualSize; i++) {
                    buffer[i] = newBuffer[i];
                }
                fut.finishFutureAndReturnData(actualSize);
            }), (exception) -> {
                SwingUtilities.invokeLater(() -> {
                    Utils.showErrorMessage("Error while try to read buffer from file [" + exception + "]");
                });
            });
            return future;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private Callback createResultCallback(ListenableFutureTaskWithData future, ReceivedDataProcessor receivedDataProcessor) {
        return (requestObject) -> {
            try {
                ByteArrayParserFormatter parser = new ByteArrayParserFormatter(((Request) requestObject).getBytes());
                String ok = parser.receiveString();
                if (ok.equals(ERROR)) {
                    String errorMessage = parser.receiveString();
                    future.finishFutureAndReturnException(new IllegalArgumentException("Client send a message [" + errorMessage + "]"));
                    System.out.println("Client send a message [" + errorMessage + "]");
                    return;
                } else if (!ok.equals(OK)) {
                    future.finishFutureAndReturnException(new IllegalArgumentException("Client has responded not with [OK], but with [" + ok + "]"));
                    return;
                }

                if (receivedDataProcessor == null) {
                    future.finishFutureAndReturnData(Boolean.TRUE);
                } else {
                    receivedDataProcessor.onFinish(future, parser);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                future.finishFutureAndReturnException(ex);
            }
        };
    }

    private static interface ReceivedDataProcessor {

        public void onFinish(ListenableFutureTaskWithData future, ByteArrayParserFormatter parser) throws IOException;
    }
}
