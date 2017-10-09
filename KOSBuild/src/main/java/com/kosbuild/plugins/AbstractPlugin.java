package com.kosbuild.plugins;

import com.kosbuild.config.BuildContext;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;

/**
 * @author Dmitry
 */
public abstract class AbstractPlugin {

    public static final String CLEAN = "clean";

    public static final String VALIDATE = "validate";
    public static final String COMPILE_SUBMODULE = "compile_submodule";
    public static final String COMPILE = "compile";
    public static final String TEST = "test";
    public static final String PACKAGE = "package";
    public static final String VERIFY = "verify";
    public static final String INSTALL = "install";
    public static final String DEPLOY = "deploy";

    public abstract String name();

    public abstract String version();

    public abstract boolean call(BuildContext buildContext, PluginConfig pluginConfig, String currentStep) throws Exception;

    public void init() {
    }

    ;

    public abstract String[] getStages();

    protected String getStringProperty(String key, JsonObject config, String defaultValue) {
        if (config.contains(key)) {
            return config.getElementByName(key).getAsString();
        }
        return defaultValue;
    }

    protected Boolean getBooleanProperty(String key, JsonObject config, Boolean defaultValue) {
        if (!config.contains(key)) {
            return defaultValue;
        }

        return config.getElementByName(key).getAsBoolean();

    }

    protected String[] getStringArrayProperty(String key, JsonObject config, String[] defaultValue) {
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
