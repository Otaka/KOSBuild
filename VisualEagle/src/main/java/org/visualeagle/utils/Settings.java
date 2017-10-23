package org.visualeagle.utils;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.visualeagle.gui.mainwindow.MainWindow;

/**
 * @author Dmitry
 */
public class Settings {

    private static final Preferences preferences = Preferences.userNodeForPackage(MainWindow.class);

    public static String getStringProperty(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    public static void putStringProperty(String key, String value) {
        preferences.put(key, value);
    }

    public static void putIntProperty(String key, int value) {
        preferences.putInt(key, value);
    }

    public static void putBooleanProperty(String key, boolean value) {
        preferences.putBoolean(key, value);
    }

    public static int getIntProperty(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public static void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }
    public static void clear() {
        try {
            preferences.clear();
            preferences.flush();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }

}
