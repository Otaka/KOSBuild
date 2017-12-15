package org.visualeagle.gui.remotewindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.gui.remotewindow.fileprovider.FileSystemType;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class RemoteCommanderWindow extends JDialog {

    private FilePanel leftPanel;
    private FilePanel rightPanel;

    public RemoteCommanderWindow() {
        super((MainWindow) Lookup.get().get(MainWindow.class), "Remote Commander", ModalityType.MODELESS);
        init();
    }

    private void init() {
        setIconImage(ImageManager.get().getImage("eagle"));

        setLayout(new BorderLayout(5, 5));
        JPanel filePanelContainer = new JPanel();
        filePanelContainer.setLayout(new GridLayout(1, 2, 5, 5));
        leftPanel = createFilePanel();
        rightPanel = createFilePanel();
        leftPanel.setOppositePanel(rightPanel);
        rightPanel.setOppositePanel(leftPanel);
        filePanelContainer.add(leftPanel);
        filePanelContainer.add(rightPanel);
        SwingUtilities.invokeLater(() -> {
            leftPanel.changeFileProvider(FileSystemType.LOCAL);
            rightPanel.changeFileProvider(FileSystemType.LOCAL);
        });

        add(filePanelContainer, BorderLayout.CENTER);
        getRootPane().putClientProperty("name", "remoteCommander");
        Lookup.get().get(MainWindow.class).getWindowLocationService().setInitialState(this, "10%", "10%", "50%", "50%", false);
        Lookup.get().get(MainWindow.class).getWindowLocationService().register(this);
        if (!(Boolean) getRootPane().getClientProperty("userChangedPosition")) {
            setLocationRelativeTo(Lookup.get().get(MainWindow.class));
        }
    }

    private FilePanel createFilePanel() {
        FilePanel filePanel = new FilePanel();
        filePanel.setOpaque(true);
        filePanel.setBackground(Color.RED);
        return filePanel;
    }
}
