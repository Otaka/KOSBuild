package org.visualeagle.gui.remotewindow;

import com.asyncsockets.Callback;
import com.asyncsockets.ListenableFutureTask;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.visualeagle.gui.remotewindow.fileprovider.AbstractFileProvider;
import org.visualeagle.gui.remotewindow.fileprovider.FileSystemType;
import org.visualeagle.gui.remotewindow.fileprovider.LocalFileSystemFileProvider;
import org.visualeagle.gui.remotewindow.fileprovider.RemoteFileSystemFileProvider;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.LongRunningTask;
import org.visualeagle.utils.LongRunningTaskWithDialog;
import org.visualeagle.utils.Utils;
import org.visualeagle.utils.atable.ATable;

public class FilePanel extends JPanel {

    private FilePanel oppositePanel;
    private AbstractFileProvider fileProvider;
    private Map<FileSystemType, AbstractFileProvider> fileProvidersCacheMap = new HashMap<>();
    private JComboBox<FileSystemType> fileSystemSelectorCB;
    private ATable<RFileWrapper> fileList;
    private final RFile parentRFile;
    private JTextField pathTextField;

    public FilePanel() {
        setLayout(new BorderLayout(5, 5));
        add(createHeader(), BorderLayout.NORTH);
        parentRFile = createGoToParentRFile();
        try {
            fileList = ATable.createATable(RFileWrapper.class);
        } catch (Exception ex) {
            throw new RuntimeException("Exception while try to create table", ex);
        }

        fileList.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel labelComponent = (JLabel) component;
                if (column == 0) {
                    RFileWrapper file = ((ATable<RFileWrapper>) table).getRow(row);
                    if (file.getFile().isDirectory()) {
                        labelComponent.setIcon(ImageManager.get().getIcon("folder"));
                    } else {
                        labelComponent.setIcon(ImageManager.get().getIconForFile("file", file.getFile().getExtension()));
                    }
                } else {
                    labelComponent.setIcon(null);
                }
                return component;
            }
        });

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (fileList.getSelectedRow() != -1) {
                        onFileSelected(fileList.getSelectedObject().getFile());
                    }
                }
            }
        });

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    /* int row = fileList.locationToIndex(e.getPoint());
                    //first check if this row is in the list of already selected rows
                    if (!ArrayUtils.contains(list.getSelectedIndices(), row)) {
                        list.setSelectedIndex(row);
                    }*/
                }
            }
        });

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 && (e.getClickCount() % 2) == 0) {
                    if (fileList.getSelectedRow() != -1) {
                        onFileSelected(fileList.getSelectedObject().getFile());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        fileList.setFillsViewportHeight(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane);
        fileList.setComponentPopupMenu(createPopupMenu());
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(this::threadedCopySelectedFiles);
        popupMenu.add(copy);

        JMenuItem createFolder = new JMenuItem("Create Folder");
        createFolder.addActionListener(this::createFolder);
        popupMenu.add(createFolder);

        JMenuItem deleteFolder = new JMenuItem("Delete");
        deleteFolder.addActionListener(this::deleteSelectedFiles);
        popupMenu.add(deleteFolder);

        JMenuItem rename = new JMenuItem("Rename");
        rename.addActionListener(this::renameSelectedFile);
        popupMenu.add(rename);

        return popupMenu;
    }

    private JPanel createHeader() {
        JPanel headPanel = new JPanel();
        headPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));
        fileSystemSelectorCB = new JComboBox<>();
        headPanel.add(fileSystemSelectorCB);
        fileSystemSelectorCB.addItem(FileSystemType.LOCAL);
        fileSystemSelectorCB.addItem(FileSystemType.REMOTE);
        fileSystemSelectorCB.setSelectedIndex(0);
        fileSystemSelectorCB.addActionListener((ActionEvent e) -> {
            FileSystemType fileSystemType = (FileSystemType) fileSystemSelectorCB.getSelectedItem();
            changeFileProvider(fileSystemType);
        });

        pathTextField = new JTextField();
        pathTextField.setMinimumSize(new Dimension(10, 25));
        pathTextField.setPreferredSize(new Dimension(500, 25));
        pathTextField.setEditable(false);
        headPanel.add(pathTextField);
        return headPanel;
    }

    private RFile createGoToParentRFile() {
        return new RFile(null, "..", 0, true, 0, null);
    }

    private void onFileSelected(RFile file) {
        if (file == parentRFile) {
            goToParent();
        } else if (file.isDirectory()) {
            enterInDirectory(file);
        } else {
            Utils.showErrorMessage("Viewing of the file is not implemented");
        }
    }

    private void goToParent() {
        RFile currentFolder = fileProvider.getCurrentFolder();
        if (currentFolder != null) {
            RFile parentFile = currentFolder.getParentRFile();
            setCurrentFolder(parentFile);
            fillFileList(() -> {
                String name = currentFolder.getName();
                for (int i = 0; i < fileList.getSelectedObjects().size(); i++) {
                    RFileWrapper file = (RFileWrapper) fileList.getSelectedObjects().get(i);
                    if (name.equals(file.getName())) {
                        fileList.setSelectedRow(i);
                        fileList.ensureRowIsVisible(i);
                        break;
                    }
                }
            });
        }
    }

    public void setOppositePanel(FilePanel oppositePanel) {
        this.oppositePanel = oppositePanel;
    }

    public FilePanel getOppositePanel() {
        return oppositePanel;
    }

    private void enterInDirectory(RFile file) {
        setCurrentFolder(file);
        fillFileList(null);
    }

    private void setCurrentFolder(RFile file) {
        fileProvider.setCurrentFolder(file);
        updatePathTextField();
    }

    public RFile getCurrentFolder() {
        return fileProvider.getCurrentFolder();
    }

    private void updatePathTextField() {
        RFile currentFolder = fileProvider.getCurrentFolder();
        if (currentFolder != null) {
            pathTextField.setText(currentFolder.getFullPath());
        } else {
            pathTextField.setText("");
        }
    }

    public void changeFileProvider(FileSystemType fileSystemType) {
        if (fileProvidersCacheMap.containsKey(fileSystemType)) {
            fileProvider = fileProvidersCacheMap.get(fileSystemType);
        } else {
            fileProvider = fileSystemType == FileSystemType.LOCAL ? new LocalFileSystemFileProvider() : new RemoteFileSystemFileProvider();
            fileProvidersCacheMap.put(fileSystemType, fileProvider);
        }

        fillFileList(null);
        updatePathTextField();
    }

    private Callback<Throwable> createErrorCallback() {
        return (Throwable result) -> {
            result.printStackTrace();
            Utils.showErrorMessage("Error while do the file list.\n" + ExceptionUtils.getRootCauseMessage(result));
        };
    }

    private void fillFileList(Runnable onAfterFileList) {
        ListenableFutureTask<List<RFile>> future;
        if (fileProvider.getCurrentFolder() == null) {
            future = fileProvider.listRoots();
        } else {
            future = fileProvider.listFiles(fileProvider.getCurrentFolder());
        }

        future.setOnError(createErrorCallback());
        future.setOnFinish((List<RFile> result) -> {
            SwingUtilities.invokeLater(() -> {
                fileList.clear();
                sortFileList(result);

                RFile currentFolder = fileProvider.getCurrentFolder();
                if (currentFolder != null) {
                    fileList.addData(new RFileWrapper(parentRFile));
                }

                for (RFile file : result) {
                    fileList.addData(new RFileWrapper(file));
                }
                fileList.fireDataChanged();
                if (!result.isEmpty()) {
                    fileList.setSelectedRow(0);
                }
                if (onAfterFileList != null) {
                    onAfterFileList.run();
                }
            });
        });
    }

    private void sortFileList(List<RFile> files) {
        Collections.sort(files, (RFile o1, RFile o2) -> {
            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            } else {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    private void threadedCopySelectedFiles(ActionEvent e) {
        if (fileList.getSelectedRowCount() == 0) {
            Utils.showErrorMessage("Please select file that will be copied");
            return;
        }
        if (oppositePanel.getCurrentFolder() == null) {
            Utils.showErrorMessage("Current folder is not set in opposite panel");
            return;
        }

        List<RFile> selectedFiles = rfileWrapperListToRFileList(fileList.getSelectedObjects());
        LongRunningTaskWithDialog longRunningTask = new LongRunningTaskWithDialog(SwingUtilities.getWindowAncestor(this), new LongRunningTask() {
            @Override
            public Object run(LongRunningTaskWithDialog dialog) throws Exception {
                internalCopyFiles(dialog, selectedFiles);
                return true;
            }

            @Override
            public void onError(LongRunningTaskWithDialog dialog, Exception ex) {
                System.out.println("Errors while copy files");
            }

            @Override
            public void onDone(LongRunningTaskWithDialog dialog, Object result) {
                oppositePanel.fillFileList(null);
                System.out.println("Done copy files");
            }
        });

        longRunningTask.start();
    }

    private void createFolder(ActionEvent e) {
        try {
            String newName = (String) JOptionPane.showInputDialog(this, "Enter directory name",
                    "Create folder", QUESTION_MESSAGE, null, null,
                    "new_folder");
            if (newName == null) {
                return;
            }

            newName = newName.trim();
            if (newName.isEmpty() || newName.contains("?") || newName.contains("\\") || newName.contains("/")) {
                Utils.showErrorMessage("Wrong name [" + newName + "]. It should not be empty and should not contain special characters");
                return;
            }

            ListenableFutureTask<Boolean> result = fileProvider.createFolder(fileProvider.getCurrentFolder(), newName);

            Boolean resultFlag = result.get();
            if (Objects.equals(resultFlag, Boolean.FALSE)) {
                Utils.showErrorMessage("Cannot create folder with name [" + newName + "]");
                return;
            }

            fillFileList(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void renameSelectedFile(ActionEvent e) {
        try {
            if (fileList.getSelectedRowCount() == 0) {
                Utils.showErrorMessage("Please select file that will be renamed");
                return;
            }

            if (fileList.getSelectedRowCount() > 1) {
                Utils.showErrorMessage("Cannot rename many files");
                return;
            }

            RFile selectedFile = fileList.getSelectedObject().getFile();
            String newName = (String) JOptionPane.showInputDialog(this, "Enter new name for file",
                    "Rename", QUESTION_MESSAGE, null, null,
                    selectedFile.getName());
            if (newName == null) {
                return;
            }

            if (newName.equals(selectedFile.getName())) {
                System.out.println("Skip renaming, because new file name is equals to old file name");
                return;
            }

            newName = newName.trim();
            if (newName.isEmpty() || newName.contains("?") || newName.contains("\\") || newName.contains("/")) {
                Utils.showErrorMessage("Wrong name [" + newName + "]. It should not be empty and should not contain special characters");
                return;
            }

            System.out.println("New file name = [" + newName + "]");
            ListenableFutureTask<Boolean> result = fileProvider.renameFile(selectedFile, newName);
            Boolean resultFlag = result.get();
            if (Objects.equals(resultFlag, Boolean.FALSE)) {
                Utils.showErrorMessage("Cannot rename file");
                return;
            }

            fillFileList(null);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            Utils.showErrorMessage("Cannot rename file\n[" + ex.getMessage() + "]");
        }
    }

    private void deleteSelectedFiles(ActionEvent e) {
        if (fileList.getSelectedRowCount() == 0) {
            Utils.showErrorMessage("Please select file that should be removed");
            return;
        }

        List<RFile> selectedFiles = rfileWrapperListToRFileList(fileList.getSelectedObjects());
        String message;
        String caption;

        if (selectedFiles.size() == 1) {
            RFile selectedFile = selectedFiles.get(0);
            message = "Are you sure, you want to remove file [" + selectedFile.getName() + "]";
            caption = "Remove file";
        } else {
            message = "Are you sure, you want to remove " + selectedFiles.size() + " files";
            caption = "Remove files";
        }

        if (JOptionPane.YES_OPTION != JOptionPane.showOptionDialog(null, message, caption, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
            return;
        }
        LongRunningTaskWithDialog longRunningTask = new LongRunningTaskWithDialog(SwingUtilities.getWindowAncestor(this), new LongRunningTask() {
            @Override
            public Object run(LongRunningTaskWithDialog dialog) throws Exception {
                internalDeleteFiles(dialog, selectedFiles);
                return true;
            }

            @Override
            public void onError(LongRunningTaskWithDialog dialog, Exception ex) {
                System.out.println("Errors while delete files");
            }

            @Override
            public void onDone(LongRunningTaskWithDialog dialog, Object result) {
                fillFileList(null);
                System.out.println("Done delete files");
            }
        });

        longRunningTask.start();
    }

    private void internalDeleteFiles(LongRunningTaskWithDialog dialog, List<RFile> selectedFiles) throws InterruptedException, ExecutionException {
        dialog.setDialogTitle("Delete file" + (selectedFiles.size() == 1 ? "" : "s"));
        dialog.setIndeterminate(true);
        dialog.setInformationMessage1("Obtain files");
        dialog.setInformationMessage1("");

        List<RFile> filesToRemove = collectFiles(selectedFiles, null, dialog, false);
        dialog.setDialogTitle("Delete file" + (filesToRemove.size() == 1 ? "" : "s"));
        dialog.setIndeterminate(false);
        dialog.setMaxProgressValue(filesToRemove.size());
        dialog.setCurrentProgressValue(0);
        dialog.setInformationMessage2("0/" + filesToRemove.size());
        for (int i = 0; i < filesToRemove.size(); i++) {
            if (dialog.isCanceled()) {
                return;
            }

            RFile fileToRemove = filesToRemove.get(i);
            dialog.setInformationMessage1(fileToRemove.getName());
            boolean skipAll = false;
            OUTER:
            while (true) {
                try {
                    fileToRemove.getFileProvider().removeFile(fileToRemove).waitForCompletion();
                    dialog.setInformationMessage2("" + (i + 1) + "/" + filesToRemove.size());
                    dialog.setCurrentProgressValue(i + 1);
                    break;
                } catch (Exception ex) {
                    if (skipAll) {
                        break;
                    }
                    Object[] options = new Object[]{"Abort", "Skip", "Retry", "Skip All"};
                    int result = JOptionPane.showOptionDialog(this, "Cannot remove file " + ex.getMessage(), "Cannot remove file",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, null);
                    switch (result) {
                        case 0:
                            //abort
                            return;
                        case 1:
                            //skip
                            break OUTER;
                        case 2:
                            //retry
                            break;
                        case 3:
                            //skip all
                            skipAll = true;
                            break OUTER;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void internalCopyFiles(LongRunningTaskWithDialog dialog, List<RFile> selectedFiles) throws InterruptedException, ExecutionException {
        dialog.setDialogTitle("Copy file" + (selectedFiles.size() == 1 ? "" : "s"));
        dialog.setIndeterminate(true);
        dialog.setInformationMessage1("Obtain files");
        dialog.setInformationMessage1("");

        List<RFile> filesToCopy = collectFiles(selectedFiles, null, dialog, true);
        dialog.setDialogTitle("Copy file" + (filesToCopy.size() == 1 ? "" : "s"));
        long totalSize = 0;
        for (RFile file : filesToCopy) {
            totalSize += file.getSize();
        }

        dialog.setIndeterminate(false);
        dialog.setMaxProgressValue(totalSize);
        dialog.setCurrentProgressValue(0);
        RFile destinationFolder = oppositePanel.getCurrentFolder();
        MutableLong currentCopiedBytesSize = new MutableLong(0);
        dialog.setInformationMessage2("0/" + filesToCopy.size());
        boolean skipAll = false;
        for (int i = 0; i < filesToCopy.size(); i++) {
            if (dialog.isCanceled()) {
                return;
            }
            RFile fileToCopy = filesToCopy.get(i);
            dialog.setInformationMessage1(Utils.convertToStringRepresentation(fileToCopy.getSize()) + "  " + fileToCopy.getName());
            while (true) {
                long currentBytesSizeValue = currentCopiedBytesSize.longValue();
                try {
                    copyFile(dialog, fileToCopy, destinationFolder, totalSize, currentCopiedBytesSize);
                    break;
                } catch (Exception ex) {
                    if (skipAll) {
                        break;
                    }
                    Object[] options = new Object[]{"Abort", "Skip", "Retry", "Skip All"};
                    int result = JOptionPane.showOptionDialog(this, "Cannot copy file " + ex.getMessage(), "Cannot copy file",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, null);
                    if (result == 0) {//abort
                        return;
                    }
                    if (result == 1) {//skip
                        break;
                    }
                    if (result == 2) {//retry
                        currentCopiedBytesSize.setValue(currentBytesSizeValue);
                    }
                    if (result == 3) {//skip all
                        skipAll = true;
                        break;
                    }
                }
            }

            dialog.setInformationMessage2("" + (i + 1) + "/" + filesToCopy.size());
        }
    }

    private void copyFile(LongRunningTaskWithDialog dialog, RFile fileToCopy, RFile destination, long totalSize, MutableLong currentCopiedSize) throws InterruptedException, ExecutionException {
        String[] currentDirSplitted = Utils.splitFilePath(getCurrentFolder());
        String[] fileToCopySplitted = Utils.splitFilePath(fileToCopy);
        String[] partsWithoutParentDirAndName = Arrays.copyOfRange(fileToCopySplitted, currentDirSplitted.length, fileToCopySplitted.length - 1);
        String fileName = fileToCopySplitted[fileToCopySplitted.length - 1];
        RFile newFile = createDestinationFilePath(destination.getFileProvider(), Utils.splitFilePath(destination), partsWithoutParentDirAndName, fileName, fileToCopy.isDirectory(), fileToCopy.getSize());
        AbstractFileProvider destinationProvider = destination.getFileProvider();
        if (newFile.isDirectory()) {
            if (!destinationProvider.exists(newFile).get()) {
                boolean result = destinationProvider.createFolder(newFile.getParentRFile(), fileName).get();
            }
        } else {
            byte[] buffer = new byte[100 * 1024];
            AbstractFileProvider sourceProvider = fileToCopy.getFileProvider();
            long readHandle = sourceProvider.openFileForReading(fileToCopy).get();
            long writeHandle = destinationProvider.openFileForWriting(newFile, false).get();
            while (true) {
                if (dialog.isCanceled()) {
                    break;
                }

                int count = sourceProvider.readFromFile(readHandle, buffer).get();
                if (count <= 0) {
                    break;
                }

                if (dialog.isCanceled()) {
                    break;
                }

                destinationProvider.writeToFile(writeHandle, buffer, count).waitForCompletion();
                currentCopiedSize.add(count);
                dialog.setCurrentProgressValue(currentCopiedSize.longValue());
                dialog.setTextInProgressBar(Utils.convertToStringRepresentation(currentCopiedSize.longValue()) + "/" + Utils.convertToStringRepresentation(totalSize));
            }

            ListenableFutureTask<Boolean> destCloseFuture = destinationProvider.close(writeHandle);
            ListenableFutureTask<Boolean> sourceCloseFuture = sourceProvider.close(readHandle);
            destCloseFuture.waitForCompletion();
            sourceCloseFuture.waitForCompletion();
        }
    }

    private RFile createDestinationFilePath(AbstractFileProvider destinationFileProvider, String[] partsOfDestinationDir, String[] partOfOriginalNameAfterParentDir, String fileName, boolean isDirectory, long size) {
        String separator = destinationFileProvider.separator();
        StringBuilder path = new StringBuilder();

        for (String part : partsOfDestinationDir) {
            path.append(part);
            path.append(separator);
        }

        for (String part : partOfOriginalNameAfterParentDir) {
            path.append(part);
            path.append(separator);
        }

        RFile newRFile = new RFile(path.toString(), fileName, size, isDirectory, 0, destinationFileProvider);
        return newRFile;
    }

    private List<RFile> collectFiles(List<RFile> roots, List<RFile> output, LongRunningTaskWithDialog dialog, boolean putFolderAtFront) throws InterruptedException, ExecutionException {
        if (output == null) {
            output = new ArrayList<>();
        }

        for (RFile fileToCopy : roots) {
            if (!fileToCopy.isDirectory()) {
                output.add(fileToCopy);
            } else {
                RFile folder = fileToCopy;
                if (putFolderAtFront) {
                    output.add(folder);
                }

                ListenableFutureTask<List<RFile>> childFuture = fileProvider.listFiles(fileToCopy);
                List<RFile> children = childFuture.get();
                collectFiles(children, output, dialog, putFolderAtFront);
                if (!putFolderAtFront) {
                    output.add(folder);
                }
            }

            dialog.setInformationMessage1("Obtain files: " + output.size());
        }

        return output;
    }

    private List<RFile> rfileWrapperListToRFileList(List<RFileWrapper> list) {
        List<RFile> result = new ArrayList();
        for (RFileWrapper rfw : list) {
            result.add(rfw.getFile());
        }
        return result;
    }
}
