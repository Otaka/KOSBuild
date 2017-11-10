package org.visualeagle.gui.remotewindow;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class RemoteCommanderWindow extends JDialog{

    public RemoteCommanderWindow() {
        super( (MainWindow)Lookup.get().get(MainWindow.class), "Remote Commander",ModalityType.MODELESS);
        init();
    }

    private void init() {
        setIconImage(ImageManager.get().getImage("eagle"));
        setLocationRelativeTo(Lookup.get().get(MainWindow.class));
    }
}
