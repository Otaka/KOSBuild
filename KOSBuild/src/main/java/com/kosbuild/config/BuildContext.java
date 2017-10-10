package com.kosbuild.config;

import com.kosbuild.dependencies.Dependency;
import com.kosbuild.plugins.PluginConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry
 */
public class BuildContext {

    private String applicationName = "resultApp";
    private String version;
    private File buildFile;
    private File projectFolder;
    private Set<Dependency> dependencies = new HashSet<>();
    private List<PluginConfig> plugins = new ArrayList<>();

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public File getBuildFile() {
        return buildFile;
    }

    public void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    public File getProjectFolder() {
        return projectFolder;
    }

    public void setBuildFile(File buildFile) {
        this.buildFile = buildFile;
    }

    public List<PluginConfig> getPlugins() {
        return plugins;
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public BuildContext addDependency(Dependency dependency) {
        dependencies.add(dependency);
        return this;
    }

    public BuildContext addPlugin(PluginConfig pluginConfig) {
        plugins.add(pluginConfig);
        return this;
    }

}
