package com.kosbuild.compiler;

import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.Dependency;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.utils.StringBuilderWithPadding;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * @author sad
 */
public class Installer {

    public Object installStaticLibrary(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        File targetFolder = CompilerUtils.getTargetFolder(buildContext.getProjectFolder());
        File resultArtifactFile = new File(targetFolder, buildContext.getApplicationName() + ".a");
        if (!resultArtifactFile.exists()) {
            throw new IllegalStateException("Cannot find result artifact [" + resultArtifactFile.getAbsolutePath() + "]");
        }

        Dependency dependency = new Dependency();
        dependency.setName(buildContext.getApplicationName());
        dependency.setVersion(buildContext.getVersion());
        dependency.setCompiler(pluginConfig.getName() + ":" + pluginConfig.getVersion());
        File pathToDependencyFolder = new DependencyExtractor().getPathToDependencyInLocalRepository(dependency, "packages");
        if (!pathToDependencyFolder.exists()) {
            pathToDependencyFolder.mkdirs();
        } else {
            clearStaticLibLocalRepository(pathToDependencyFolder);
        }

        copyResultArtifact(resultArtifactFile, pathToDependencyFolder);
        createIncludeFolderLink(buildContext, pathToDependencyFolder);
        createPackageInfoFile(pathToDependencyFolder, buildContext);
        createPackageProperties(pathToDependencyFolder);
        return AbstractPlugin.DONE_RESULT;
    }

    private void copyResultArtifact(File resultArtifactFile, File pathToDependencyFolder) throws IOException {
        File repositoryLibsFolder = new File(pathToDependencyFolder, "libs");

        repositoryLibsFolder.mkdir();
        String libFileName = "lib" + resultArtifactFile.getName();
        FileUtils.copyFile(resultArtifactFile, new File(repositoryLibsFolder, libFileName));
    }

    private void createIncludeFolderLink(BuildContext buildContext, File pathToDependencyFolder) throws IOException {
        File repositoryIncludeFolder = new File(pathToDependencyFolder, "include");
        repositoryIncludeFolder.mkdir();
        File includeFilesLink = new File(repositoryIncludeFolder, "includeFolder.lnk");
        String value = "path=" + escapePropertiesFileString(CompilerUtils.fixPath(CompilerUtils.getHeadersFolder(buildContext).getAbsolutePath()));
        FileUtils.writeStringToFile(includeFilesLink, value, "UTF-8");
    }

    private String escapePropertiesFileString(String value) {
        return value.replace("\\", "\\\\");
    }

    private void createPackageProperties(File pathToDependencyFolder) throws IOException {
        File packagePropertiesFile = new File(pathToDependencyFolder, "package.properties");
        String value = "loaded=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        FileUtils.writeStringToFile(packagePropertiesFile, value, "UTF-8");
    }

    private List<Dependency> getTransitiveDependencies(BuildContext buildContext) {
        List<Dependency> transitiveDependencies = new ArrayList<>();
        for (Dependency dep : buildContext.getDependencies()) {
            if (dep.isTransitive()) {
                transitiveDependencies.add(dep);
            }
        }
        return transitiveDependencies;
    }

    private void createPackageInfoFile(File pathToDependencyFolder, BuildContext buildContext) throws IOException {
        StringBuilderWithPadding sb = new StringBuilderWithPadding("    ");
        sb.append("{\n");
        sb.incLevel();

        List<Dependency> transitiveDependencies = getTransitiveDependencies(buildContext);
        if (!transitiveDependencies.isEmpty()) {
            sb.append("dependencies:[\n");
            sb.incLevel();
            boolean first = true;
            for (Dependency dep : transitiveDependencies) {
                if (first == false) {
                    sb.append(",\n");
                }

                sb.append("{\n");
                sb.incLevel();

                sb.append("name:\"").append(dep.getName() + ":" + dep.getVersion()).append("\",\n");
                sb.append("compiler:\"").append(dep.getCompiler() + "\"\n");

                sb.decLevel();
                sb.append("}\n");
                first = true;
            }

            sb.decLevel();
            sb.append("],\n");
        }

        sb.append("type:'staticlib'\n");

        sb.decLevel();
        sb.append("}");
        File packageInfoFile = new File(pathToDependencyFolder, "packageInfo.json");
        FileUtils.writeStringToFile(packageInfoFile, sb.toString(), "UTF-8");
    }

    private void clearStaticLibLocalRepository(File pathToDependencyRepository) throws IOException {
        FileUtils.cleanDirectory(pathToDependencyRepository);
    }
}
