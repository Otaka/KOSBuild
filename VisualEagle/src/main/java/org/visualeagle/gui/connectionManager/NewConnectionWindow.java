package org.visualeagle.gui.connectionManager;

import javax.swing.JDialog;
import javax.swing.JFrame;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class NewConnectionWindow extends JDialog {

    public NewConnectionWindow() {
        setModal(true);
        setTitle("Connection wizard");
        setLocationRelativeTo(Lookup.get().get(MainWindow.class));
    }
    
}
