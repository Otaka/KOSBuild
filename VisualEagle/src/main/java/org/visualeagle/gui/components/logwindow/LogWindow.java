package org.visualeagle.gui.components.logwindow;

import java.awt.BorderLayout;
import javax.swing.JInternalFrame;

/**
 * @author Dmitry
 */
public class LogWindow extends JInternalFrame {

    public LogWindow(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        initGui();
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 0));
    }

}
