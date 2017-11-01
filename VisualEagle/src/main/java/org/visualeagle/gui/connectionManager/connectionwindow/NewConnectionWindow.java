package org.visualeagle.gui.connectionmanager.connectionwindow;

import javax.swing.JDialog;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.gui.connectionManager.connectionwindow.ConnectAsClientPanel;
import org.visualeagle.gui.connectionManager.connectionwindow.ConnectAsServerPanel;

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
        connectionTypeChooser.setOnSelectEvent(this::onSelectConnectionType);
        add(connectionTypeChooser);
        pack();
        setResizable(false);

    }

    private void removeSelectionPanel() {
        connectionTypeChooser.setOnSelectEvent(null);
        remove(connectionTypeChooser);

    }

    private void onSelectConnectionType(ConnectionType connectionType) {
        removeSelectionPanel();
        invalidate();
        repaint();
        
        if (connectionType == ConnectionType.CLIENT) {
            ConnectAsClientPanel newPanel = new ConnectAsClientPanel();
            add(newPanel);
        } else {
            ConnectAsServerPanel newPanel = new ConnectAsServerPanel();
            add(newPanel);
        }

        revalidate();
        repaint();
        pack();
        
    }
}
