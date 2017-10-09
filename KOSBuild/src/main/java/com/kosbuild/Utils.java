package com.kosbuild;

import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import java.io.File;
import java.net.URISyntaxException;

/**
 * @author Dmitry
 */
public class Utils {

    public static enum OperationSystem {
        Windows, Linux, OsX
    }

    public static File getAppFolder() {
        try {
            return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static OperationSystem getOperationSystem() {
        String osString = System.getProperty("os.name").toLowerCase();
        if (osString.contains("win")) {
            return OperationSystem.Windows;
        }
        if (osString.contains("nix") || osString.contains("nux") || osString.contains("aix")) {
            return OperationSystem.Linux;
        }
        if (osString.contains("mac")) {
            return OperationSystem.OsX;
        }
        throw new IllegalArgumentException("Operation system [" + osString + "] is not supported");
    }

    public static String getStringProperty(String key, JsonObject config, String defaultValue) {
        if (config.contains(key)) {
            return config.getElementByName(key).getAsString();
        }
        return defaultValue;
    }

    public static Boolean getBooleanProperty(String key, JsonObject config, Boolean defaultValue) {
        if (!config.contains(key)) {
            return defaultValue;
        }

        return config.getElementByName(key).getAsBoolean();

    }

    public static String[] getStringArrayProperty(String key, JsonObject config, String[] defaultValue) {
        if (!config.contains(key)) {
            return defaultValue;
        }

        JsonElement e = config.getElementByName(key);
        if (!e.isArray()) {
            throw new IllegalArgumentException("[" + key + "] expected to be array of strings, but it is [" + e.getClass().getSimpleName() + "]");
        }

        JsonArray array = e.getAsArray();
        String[] values = new String[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = array.get(i).getAsString();
        }

        return values;
    }
}
