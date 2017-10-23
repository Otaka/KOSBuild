package org.visualeagle;

import javax.swing.SwingUtilities;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.utils.GuiUtils;

/**
 * @author Dmitry
 */
public class Main {
    public static void main(String[] args){
        System.out.println("Started Visual Eagle");
        new Main().start();
    }
    
    public void start() {
       
        GuiUtils.setSystemLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
