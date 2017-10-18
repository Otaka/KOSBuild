package com.kosbuild.plugins;

import java.util.List;

/**
 * @author sad
 */
public class PluginResults {

    private List<PluginResult> results;

    public PluginResults(List<PluginResult> results) {
        this.results = results;
    }

    public List<PluginResult> getResults() {
        return results;
    }

}
