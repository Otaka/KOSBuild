package com.kosbuild.plugins;

/**
 * @author sad
 */
public class PluginResult {

    private String pluginName;
    private Object value;

    public PluginResult(String pluginName, Object value) {
        this.pluginName = pluginName;
        this.value = value;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Object getValue() {
        return value;
    }

}
