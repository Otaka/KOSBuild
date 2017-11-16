package org.visualeagle.gui.remotewindow;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.visualeagle.gui.remotewindow.fileprovider.AbstractFileProvider;

/**
 * @author sad
 */
public class FilePanel extends JPanel {

    private RFile currentFolder;
    private FilePanel oppositePanel;
    private AbstractFileProvider fileProvider;
    private JComboBox<RFile> rootsComboBox;

    public FilePanel() {
        setLayout(new BorderLayout(5, 5));
        add(createHeader(), BorderLayout.NORTH);
    }

    private JPanel createHeader() {
        JPanel headPanel = new JPanel();
        headPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        headPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        rootsComboBox = new JComboBox<>();
        headPanel.add(rootsComboBox);
        rootsComboBox.addActionListener((ActionEvent e) -> {
            RFile selectedRoot = (RFile) rootsComboBox.getSelectedItem();
            rootChanged(selectedRoot);
        });

        return headPanel;
    }

    private void rootChanged(RFile newRoot) {
        currentFolder = newRoot;
        folderChanged();
    }

    public void setFileProvider(AbstractFileProvider fileProvider) {
        this.fileProvider = fileProvider;
        fileProviderChanged();
        folderChanged();
    }

    private void folderChanged() {
        List<RFile>files=fileProvider.listFiles(currentFolder);
    }

    private void fileProviderChanged() {
        List<RFile> roots = fileProvider.listRoots();
        rootsComboBox.removeAllItems();
        for (RFile file : roots) {
            rootsComboBox.addItem(file);
        }

        currentFolder = roots.get(0);
    }

    public void setOppositePanel(FilePanel oppositePanel) {
        this.oppositePanel = oppositePanel;
    }

    public FilePanel getOppositePanel() {
        return oppositePanel;
    }

    public RFile getCurrentFolder() {
        return currentFolder;
    }

}
