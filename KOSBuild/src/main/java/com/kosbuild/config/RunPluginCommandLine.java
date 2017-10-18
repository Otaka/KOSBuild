package com.kosbuild.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class RunPluginCommandLine {

    private String pluginNameVersion;
    private List<String> runOnThisSteps;

    public void setRunOnThisSteps(List<String> runOnThisSteps) {
        this.runOnThisSteps = runOnThisSteps;
    }

    public void setPluginNameVersion(String pluginNameVersion) {
        this.pluginNameVersion = pluginNameVersion;
    }

    public String getPluginNameVersion() {
        return pluginNameVersion;
    }

    public List<String> getRunOnThisSteps() {
        return runOnThisSteps;
    }

}
