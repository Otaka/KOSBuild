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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.visualeagle.gui.remotewindow.fileprovider.AbstractFileProvider;
import org.visualeagle.gui.remotewindow.fileprovider.FileSystemType;
import org.visualeagle.gui.remotewindow.fileprovider.LocalFileSystemFileProvider;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Utils;

public class FilePanel extends JPanel {

    private FilePanel oppositePanel;
    private AbstractFileProvider fileProvider;
    private Map<FileSystemType, AbstractFileProvider> fileProvidersCacheMap = new HashMap<>();
    private JComboBox<FileSystemType> fileSystemSelectorCB;
    private JList<RFile> fileList;
    private final RFile parentRFile;
    private JTextField pathTextField;

    public FilePanel() {
        setLayout(new BorderLayout(5, 5));
        add(createHeader(), BorderLayout.NORTH);
        parentRFile = createGoToParentRFile();

        fileList = new JList<>(new DefaultListModel());
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                RFile file = (RFile) list.getModel().getElementAt(index);
                JLabel labelComponent = (JLabel) component;
                labelComponent.setText(file.getName());
                if (file.isDirectory()) {
                    labelComponent.setIcon(ImageManager.get().getIcon("folder"));
                } else {
                    labelComponent.setIcon(ImageManager.get().getIcon("file"));
                }
                return component;
            }
        });

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (fileList.getSelectedIndex() != -1) {
                        onFileSelected(fileList.getSelectedValue());
                    }
                }
            }
        });

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 && (e.getClickCount() % 2) == 0) {
                    if (fileList.getSelectedIndex() != -1) {
                        onFileSelected(fileList.getSelectedValue());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane);
        fileList.setComponentPopupMenu(createPopupMenu());
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(this::copySelectedFiles);
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
            fillFileList();
        }
    }

    private void enterInDirectory(RFile file) {
        setCurrentFolder(file);
        fillFileList();
    }

    private void setCurrentFolder(RFile file) {
        fileProvider.setCurrentFolder(file);
        if (file != null) {
            pathTextField.setText(file.getFullPath());
        } else {
            pathTextField.setText("");
        }
    }

    public void changeFileProvider(FileSystemType fileSystemType) {
        if (fileProvidersCacheMap.containsKey(fileSystemType)) {
            fileProvider = fileProvidersCacheMap.get(fileSystemType);
        } else {
            fileProvider = fileSystemType == FileSystemType.LOCAL ? new LocalFileSystemFileProvider() : new LocalFileSystemFileProvider();
            fileProvidersCacheMap.put(fileSystemType, fileProvider);
        }

        fillFileList();
    }

    private Callback<Throwable> createErrorCallback() {
        return (Throwable result) -> {
            result.printStackTrace();
            Utils.showErrorMessage("Error while do the file list.\n" + ExceptionUtils.getRootCauseMessage(result));
        };
    }

    private void fillFileList() {
        DefaultListModel fileListModel = (DefaultListModel) fileList.getModel();
        ListenableFutureTask<List<RFile>> future;
        if (fileProvider.getCurrentFolder() == null) {
            future = fileProvider.listRoots();
        } else {
            future = fileProvider.listFiles(fileProvider.getCurrentFolder());
        }

        future.setOnError(createErrorCallback());
        future.setOnFinish((List<RFile> result) -> {
            SwingUtilities.invokeLater(() -> {
                fileListModel.clear();
                sortFileList(result);

                RFile currentFolder = fileProvider.getCurrentFolder();
                if (currentFolder != null) {
                    fileListModel.addElement(parentRFile);
                }

                for (RFile file : result) {
                    fileListModel.addElement(file);
                }

                if (!result.isEmpty()) {
                    fileList.setSelectedIndex(0);
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

    public void setOppositePanel(FilePanel oppositePanel) {
        this.oppositePanel = oppositePanel;
    }

    public FilePanel getOppositePanel() {
        return oppositePanel;
    }

    private void copySelectedFiles(ActionEvent e) {

    }

    private void createFolder(ActionEvent e) {

    }

    private void deleteSelectedFiles(ActionEvent e) {

    }

    private void renameSelectedFile(ActionEvent e) {
        if (fileList.getSelectedIndices().length == 0) {
            Utils.showErrorMessage("Please select file that will be renamed");
            return;
        }
        if (fileList.getSelectedIndices().length > 1) {
            Utils.showErrorMessage("Cannot rename many files");
            return;
        }

        RFile selectedFile = fileList.getSelectedValue();
        String name = (String) JOptionPane.showInputDialog(this, "Please write new name for file",
                "Rename", QUESTION_MESSAGE, null, null,
                selectedFile.getName());
        System.out.println("New file name = [" + name + "]");
    }
}
