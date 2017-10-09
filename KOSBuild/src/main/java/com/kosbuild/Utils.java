package com.kosbuild;

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
}
