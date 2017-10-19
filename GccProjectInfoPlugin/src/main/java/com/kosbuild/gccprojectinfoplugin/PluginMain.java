package com.kosbuild.gccprojectinfoplugin;

import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.Dependency;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 * @author sad
 */
public class PluginMain extends AbstractPlugin {

    private static final Logger log = Utils.getLogger();

    @Override
    public String name() {
        return "gccProjectInfo";
    }

    @Override
    public String version() {
        return "0.1";
    }

    @Override
    public Object call(BuildContext buildContext, PluginConfig pluginConfig, String currentStep) throws Exception {
        ProjectInfo projectInfo = new ProjectInfo();
        DependencyExtractor dependencyExtractor = new DependencyExtractor();
        projectInfo.setBuildFile(buildContext.getBuildFile());
        projectInfo.getSourcePaths().add(Utils.concatPaths(buildContext.getProjectFolder().getAbsolutePath(), "src", "sources"));
        projectInfo.setIncludePath(Utils.concatPaths(buildContext.getProjectFolder().getAbsolutePath(), "src", "headers"));
        
        for (Dependency dependency : buildContext.getDependencies()) {
            File dependencyFolder = dependencyExtractor.getPathToPackageDependencyAndLoadIfNotExists(dependency);
            processIncludeFolder(projectInfo.getDependenciesIncludePaths(), buildContext, dependency, dependencyFolder);
            File libsFolder = new File(dependencyFolder.getAbsoluteFile(), "libs");
            projectInfo.getDependenciesLibraryPaths().add(libsFolder.getAbsolutePath());
            for (File file : libsFolder.listFiles()) {
                String fileName = file.getName();
                if (fileName.toLowerCase().endsWith(".a")) {
                    if (!fileName.startsWith("lib")) {
                        log.warn("Library [" + fileName + "] in dependency [" + dependency + "] should start with 'lib'. Skip");
                        continue;
                    }

                    projectInfo.getDependenciesLibraryNames().add(file.getName());
                }
            }
        }
        return projectInfo;
    }

    private void processIncludeFolder(List<String> includePaths, BuildContext buildContext, Dependency dependency, File dependencyFolder) throws IOException {
        File includePath = new File(dependencyFolder.getAbsoluteFile(), "include");

        File linkFile = new File(includePath, "includeFolder.lnk");
        if (!linkFile.exists()) {
            includePaths.add(includePath.getAbsolutePath());
        } else {
            Properties properties = new Properties();
            try (FileInputStream stream = new FileInputStream(linkFile)) {
                properties.load(stream);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot parse repository include link file [" + linkFile.getAbsolutePath() + "] as properties file", ex);
            }

            if (!properties.containsKey("path")) {
                throw new RuntimeException("Repository include link file [" + linkFile.getAbsolutePath() + "] does not contain [path] property");
            }

            String path = properties.getProperty("path");
            File newIncludePath = new File(path);
            if (!newIncludePath.exists()) {
                if (isRemoveBadDependencies(buildContext)) {
                    FileUtils.deleteDirectory(dependencyFolder);
                    throw new IllegalStateException("Dependency " + dependency.formatPath() + " has include link that points to non existant folder [" + newIncludePath + "]. Removed dependency from local repository\n" + "Please run again to reload it from remote repository, or make 'install' if it is your library");
                } else {
                    throw new IllegalStateException("Dependency " + dependency.formatPath() + " has include link that points to non existant folder [" + newIncludePath + "]. Fix the problem with missing folder, or run the application with -Dremovebaddependencies to remove the dependency automatically(on next execution the application will try to load it again from remote repository)");
                }
            }

            includePaths.add(newIncludePath.getAbsolutePath());
        }
    }

    private boolean isRemoveBadDependencies(BuildContext buildContext) {
        return buildContext.getBooleanCustomSetting("removebaddependencies");
    }

    @Override
    public String[] getStages() {
        return new String[]{};//no predefined steps to run plugin. Should be executed from other plugins or manually from command line
    }

}
