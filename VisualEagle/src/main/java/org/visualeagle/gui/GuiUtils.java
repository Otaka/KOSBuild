package org.visualeagle.gui;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Dmitry
 */
public class GuiUtils {

    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Can parse number in pixels, or in percent
     */
    public static int parseWidthString(String value, int maxWidth) {
        if (value.endsWith("%")) {
            value = StringUtils.removeEnd(value, "%");
            int percentValue = (int) (Integer.parseInt(value) / 100.0f * maxWidth);
            return percentValue;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static void error(String message) {
        error(null, message);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
