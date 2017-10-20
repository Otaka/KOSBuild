package com.kosbuild.config;

import java.util.List;

/**
 * @author sad
 */
public class RunPluginCommandLine {

    private String pluginNameVersion;
    private List<String> runOnSteps;

    public void setRunOnSteps(List<String> runOnSteps) {
        this.runOnSteps = runOnSteps;
    }

    public void setPluginNameVersion(String pluginNameVersion) {
        this.pluginNameVersion = pluginNameVersion;
    }

    public String getPluginNameVersion() {
        return pluginNameVersion;
    }

    public List<String> getRunOnSteps() {
        return runOnSteps;
    }

}
