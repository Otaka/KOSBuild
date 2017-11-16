package org.visualeagle.utils;

import java.awt.Component;
import java.awt.Toolkit;
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

    public static int parseWidthString(String value, Component rootComponent) {
        int maxWidth;
        if (rootComponent == null) {
            maxWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        } else {
            maxWidth = rootComponent.getWidth();
        }
        if (value.endsWith("%")) {
            value = StringUtils.removeEnd(value, "%");
            int percentValue = (int) (Integer.parseInt(value) / 100.0f * maxWidth);
            return percentValue;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static int parseHeightString(String value, Component rootComponent) {
        int maxHeight;
        if (rootComponent == null) {
            maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        } else {
            maxHeight = rootComponent.getHeight();
        }
        if (value.endsWith("%")) {
            value = StringUtils.removeEnd(value, "%");
            int percentValue = (int) (Integer.parseInt(value) / 100.0f * maxHeight);
            return percentValue;
        } else {
            return Integer.parseInt(value);
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
