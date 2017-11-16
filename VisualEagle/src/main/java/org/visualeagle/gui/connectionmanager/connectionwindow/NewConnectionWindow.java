package org.visualeagle.gui.connectionmanager.connectionwindow;

import javax.swing.JDialog;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class NewConnectionWindow extends JDialog {

    private SelectConnectionTypePanel connectionTypeChooser;

    public NewConnectionWindow() {
        init();
    }

    private void init() {
        setIconImage(ImageManager.get().getImage("eagle"));
        setModal(true);
        setTitle("Connection wizard");
        setLocationRelativeTo(Lookup.get().get(MainWindow.class));
        connectionTypeChooser = new SelectConnectionTypePanel();
        connectionTypeChooser.setOnSelectEvent(this::onStartConnection);
        add(connectionTypeChooser);
        pack();
        setResizable(false);

    }

    private void removeSelectionPanel() {
        connectionTypeChooser.setOnSelectEvent(null);
        remove(connectionTypeChooser);

    }

    private void onStartConnection(String value) {
        removeSelectionPanel();
        invalidate();
        repaint();
        ConnectAsServerPanel newPanel = new ConnectAsServerPanel();
        add(newPanel);
        revalidate();
        repaint();
        pack();
    }
}
