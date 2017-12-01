package org.visualeagle.gui.remotewindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.visualeagle.gui.remotewindow.fileprovider.AbstractFileProvider;
import org.visualeagle.gui.remotewindow.fileprovider.FileSystemType;
import org.visualeagle.gui.remotewindow.fileprovider.LocalFileSystemFileProvider;

/**
 * @author sad
 */
public class FilePanel extends JPanel {

    private FilePanel oppositePanel;
    private AbstractFileProvider fileProvider;
    private Map<FileSystemType, AbstractFileProvider> fileProvidersCacheMap = new HashMap<>();
    private JComboBox<FileSystemType> fileSystemSelectorCB;
    private JList<RFile> fileList;

    public FilePanel() {
        setLayout(new BorderLayout(5, 5));
        add(createHeader(), BorderLayout.NORTH);

        fileList = new JList<>(new DefaultListModel());
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                RFile file = (RFile) list.getModel().getElementAt(index);
                ((JLabel) component).setText(file.getName());
                return component;
            }
        });

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(fileList.getSelectedIndex()!=-1){
                        onFileSelected();
                    }
                }
            }
        });
        
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    if(fileList.getSelectedIndex()!=-1){
                        onFileSelected();
                    }
                }
            }
            
});

        add(fileList);
    }

    private JPanel createHeader() {
        JPanel headPanel = new JPanel();
        headPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        headPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        fileSystemSelectorCB = new JComboBox<>();
        headPanel.add(fileSystemSelectorCB);
        fileSystemSelectorCB.addItem(FileSystemType.LOCAL);
        fileSystemSelectorCB.addItem(FileSystemType.REMOTE);
        fileSystemSelectorCB.setSelectedIndex(0);
        fileSystemSelectorCB.addActionListener((ActionEvent e) -> {
            FileSystemType fileSystemType = (FileSystemType) fileSystemSelectorCB.getSelectedItem();
            changeFileProvider(fileSystemType);
        });

        return headPanel;
    }
    
    private void onFileSelected(){
        
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

    private void fillFileList() {
        DefaultListModel fileListModel = (DefaultListModel) fileList.getModel();
        fileListModel.clear();

        if (fileProvider.getCurrentFolder() == null) {
            for (RFile file : fileProvider.listRoots()) {
                fileListModel.addElement(file);
            }
        } else {
            for (RFile file : fileProvider.listFiles(fileProvider.getCurrentFolder())) {
                fileListModel.addElement(file);
            }
        }
    }

    public void setOppositePanel(FilePanel oppositePanel) {
        this.oppositePanel = oppositePanel;
    }

    public FilePanel getOppositePanel() {
        return oppositePanel;
    }
}
