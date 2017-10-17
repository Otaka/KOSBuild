package com.kosbuild.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sad
 */
public class BuildArguments {

    private List<File> buildFiles = new ArrayList<>();

    private List<String> stepsToExecute = new ArrayList<>();
    private Map<String, String> customArguments = new HashMap<>();
    private String logLevel = "INFO";

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Map<String, String> getCustomArguments() {
        return customArguments;
    }

    public List<File> getBuildFiles() {
        return buildFiles;
    }

    public List<String> getStepsToExecute() {
        return stepsToExecute;
    }

}
