package com.kosbuild.gccprojectinfoplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class ProjectInfo {

    private String projectName;
    private String projectVersion;
    private String includePath;
    private List<String> librariesPath = new ArrayList<>();
    private List<String> librariesNames = new ArrayList<>();

    private List<String> dependenciesIncludePaths = new ArrayList<>();
    private List<String> dependenciesLibraryPaths = new ArrayList<>();
    private List<String> dependenciesLibraryNames = new ArrayList<>();
    private List<String> sourcePaths = new ArrayList<>();
    private String buildFile;

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getIncludePath() {
        return includePath;
    }

    public void setIncludePath(String includePath) {
        this.includePath = includePath;
    }

    public List<String> getLibrariesNames() {
        return librariesNames;
    }

    public List<String> getLibrariesPath() {
        return librariesPath;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    public String getBuildFile() {
        return buildFile;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public List<String> getDependenciesIncludePaths() {
        return dependenciesIncludePaths;
    }

    public List<String> getDependenciesLibraryNames() {
        return dependenciesLibraryNames;
    }

    public List<String> getDependenciesLibraryPaths() {
        return dependenciesLibraryPaths;
    }
}
