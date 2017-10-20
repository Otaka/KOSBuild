package com.kosbuild.utils;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.jsonparser.JsonParser;
import java.io.File;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static Logger getLogger() {
        try {
            throw new RuntimeException();
        } catch (Exception ex) {
            String[] frames = ExceptionUtils.getStackFrames(ex);
            String line = frames[2];
            String className = StringUtils.substringBefore(StringUtils.substringBetween(line, "(", "."), ")");
            return LoggerFactory.getLogger(className);
        }
    }

    public static void changeLogLevel(String level) {
        level = level.toUpperCase();
        Logger logger = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (!(logger instanceof ch.qos.logback.classic.Logger)) {
            logger.error("Cannot switch logger level, becase underlying logger expected to be ch.qos.logback.classic.Logger, but it is " + logger.getClass().getName());
            return;
        }
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) logger;
        rootLogger.setLevel(Level.toLevel(level));
    }

    /**
    It is 'hack' method that will convert Object to json and then rebuild it as object instanceof 'class'</br>
    Why we need this? Plugins may return the data in some object, but you do not have this object in your class path.
    Even if you have object with the same structure, they will be not compatible
    */
    public static <T>T reconvertWithJson(Object obj, Class<T>clazz) {
        Gson gson=new Gson();
        String val=gson.toJson(obj);
        return gson.fromJson(val, clazz);
    }

    public static String concatPaths(String... pathParts) {
        StringBuilder sb = new StringBuilder();
        boolean endsWithSlash = false;
        for (int i = 0; i < pathParts.length; i++) {
            String pathPart = pathParts[i];
            pathPart = pathPart.replace("\\", "/");
            if (i == 0) {
                sb.append(pathPart);
            } else {
                if (pathPart.startsWith("/")) {
                    if (endsWithSlash == true) {
                        pathPart = pathPart.substring(1);
                    }
                } else {
                    if (endsWithSlash == false) {
                        sb.append("/");
                    }
                }

                sb.append(pathPart);
            }

            endsWithSlash = pathPart.endsWith("/");
        }

        return sb.toString();
    }
}
