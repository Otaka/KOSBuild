package com.kosbuild.dependencies;

import com.kosbuild.LifeCycleExecutor;
import com.kosbuild.config.BuildContext;
import com.kosbuild.config.Config;
import com.kosbuild.config.Repository;
import com.kosbuild.jsonparser.FieldValuePair;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.plugins.PluginManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;

/**
 * @author Dmitry
 */
public class DependencyExtractor {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    public void collectPlugins(JsonObject jsonObject, BuildContext buildContext) throws IOException {
        if (!jsonObject.contains("build")) {
            throw new IllegalArgumentException("Build file [" + buildContext.getBuildFile().getAbsolutePath() + " does not contain [build] section");
        }

        JsonObject buildSection = jsonObject.getElementByName("build").getAsObject();
        for (FieldValuePair fvp : buildSection.getElements()) {
            JsonObject pluginJsonObject = fvp.getValue().getAsObject();
            String nameAndVersionString = pluginJsonObject.getElementByName("applyPlugin", true).getAsString();
            if (!nameAndVersionString.contains(":")) {
                throw new IllegalArgumentException("Plugin name [" + nameAndVersionString + "] is in wrong format. Proper format \"NAME:VERSION\"");
            }

            JsonObject pluginConfigJson;
            if (pluginJsonObject.contains("configure")) {
                pluginConfigJson = pluginJsonObject.getElementByName("configure").getAsObject();
            } else {
                pluginConfigJson = new JsonObject();
            }

            String name = nameAndVersionString.substring(0, nameAndVersionString.indexOf(':'));
            String version = nameAndVersionString.substring(nameAndVersionString.indexOf(':') + 1);
            PluginConfig pluginConfig = new PluginConfig();
            pluginConfig.setName(name);
            pluginConfig.setVersion(version);
            pluginConfig.setOverrideRunOnSteps(parseOverrideStepJson(pluginJsonObject));
            File folderWithPlugin = getPathToPluginDependencyAndLoadIfNotExists(new Dependency().setName(name).setVersion(version));
            pluginConfig.setPluginLocalRepositoryFolder(folderWithPlugin);
            AbstractPlugin pluginObject = loadPlugin(folderWithPlugin);
            pluginConfig.setDynamicPluginObject(pluginObject);
            pluginConfig.setConfig(pluginConfigJson);
            pluginObject.init();
            buildContext.addPlugin(pluginConfig);
        }
    }

    private AbstractPlugin loadPlugin(File pluginLocalRepositoryFolder) throws IOException {
        return PluginManager.get().loadPlugin(pluginLocalRepositoryFolder);
    }

    private String[] parseOverrideStepJson(JsonObject pluginJsonObject) {
        if (!pluginJsonObject.contains("runOnStep")) {
            return null;
        }

        JsonElement runOnStep = pluginJsonObject.getElementByName("runOnStep");
        if (runOnStep.isPrimitive()) {
            return new String[]{getAndValidateBuildStep(runOnStep.getAsString())};
        }

        if (runOnStep.isArray()) {
            JsonArray runOnStepArray = runOnStep.getAsArray();
            String[] steps = new String[runOnStepArray.size()];
            for (int i = 0; i < steps.length; i++) {
                JsonElement element = runOnStepArray.getElements().get(i);
                String step = element.getAsString();
                steps[i] = getAndValidateBuildStep(step);
            }

            return new String[]{getAndValidateBuildStep(runOnStep.getAsString())};
        }

        throw new IllegalArgumentException("Wrong [runOnStep] property format. Expected array, or string, but found [" + runOnStep.getClass().getSimpleName() + "]");
    }

    private String getAndValidateBuildStep(String stepString) {
        stepString = stepString.trim();
        if (LifeCycleExecutor.arrayIndexOfIgnoreCase(LifeCycleExecutor.cleanStage, stepString) >= 0) {
            return stepString;
        }
        if (LifeCycleExecutor.arrayIndexOfIgnoreCase(LifeCycleExecutor.buildStage, stepString) >= 0) {
            return stepString;
        }
        throw new IllegalArgumentException("[runOnStep] property contains wrong step [" + stepString + "]");
    }

    public void collectDependencies(JsonObject jsonObject, BuildContext buildContext) {
        if (jsonObject.contains("dependencies")) {
            if (!jsonObject.getElementByName("dependencies").isArray()) {
                throw new IllegalArgumentException("[dependencies] section should be array of objects, but found [" + jsonObject.getElementByName("dependencies").getClass().getName() + "]");
            }

            JsonArray dependencies = jsonObject.getElementByName("dependencies").getAsArray();
            for (JsonElement element : dependencies.getElements()) {
                JsonObject dep = element.getAsObject();
                Dependency dependency = new Dependency();
                String compiler = dep.getElementByName("compiler").getAsString();
                String nameAndVersion = dep.getElementByName("name").getAsString();
                boolean transitive = false;
                if (dep.contains("transitive")) {
                    transitive = dep.getElementByName("transitive").getAsBoolean();
                }
                
                boolean includeWithoutPrefix=false;
                if (dep.contains("includeWithoutPrefix")) {
                    includeWithoutPrefix = dep.getElementByName("includeWithoutPrefix").getAsBoolean();
                }

                if (!nameAndVersion.contains(":")) {
                    throw new IllegalArgumentException("Dependency name [" + nameAndVersion + "] is in wrong format. Proper format \"NAME:VERSION\"");
                }

                String name = nameAndVersion.substring(0, nameAndVersion.indexOf(':'));
                String version = nameAndVersion.substring(nameAndVersion.indexOf(':') + 1);
                dependency.setCompiler(compiler);
                dependency.setName(name);
                dependency.setVersion(version);
                dependency.setTransitive(transitive);
                dependency.setIncludeWithoutPrefix(includeWithoutPrefix);
                buildContext.addDependency(dependency);
            }
        }

        for (Dependency dependency : buildContext.getDependencies()) {
            processDependency(dependency, "packages");
        }
    }

    public File getPathToPluginDependencyAndLoadIfNotExists(Dependency dep) {
        if (!isDependencyExistsInLocalRepository(dep, "plugins")) {
            processDependency(dep, "plugins");
        }

        return getPathToDependencyInLocalRepository(dep, "plugins");
    }

    public File getPathToPackageDependencyAndLoadIfNotExists(Dependency dep) {
        if (!isDependencyExistsInLocalRepository(dep, "packages")) {
            processDependency(dep, "packages");
        }

        return getPathToDependencyInLocalRepository(dep, "packages");
    }

    private File getPathToDependencyInLocalRepository(Dependency dependency, String category) {
        String localRepositoryPath = Config.get().getLocalRepository().getPath();
        File localRepositoryDependencyFolder = new File(localRepositoryPath + category + "/", dependency.formatPath());
        return localRepositoryDependencyFolder;
    }

    private boolean isDependencyExistsInLocalRepository(Dependency dependency, String category) {
        File localRepositoryDependencyFolder = getPathToDependencyInLocalRepository(dependency, category);
        return !(!localRepositoryDependencyFolder.exists() || !new File(localRepositoryDependencyFolder, "package.properties").exists());
    }

    private void processDependency(Dependency dependency, String category) {
        if (!isDependencyExistsInLocalRepository(dependency, category)) {
            downloadDependency(getPathToDependencyInLocalRepository(dependency, category), dependency, category);
        }
    }

    private void downloadDependency(File localRepositoryDependencyFolder, Dependency dep, String category) {
        InputStream stream = null;
        for (Repository remoteRep : Config.get().getRemoteRepositories()) {
            String repPath = remoteRep.getPath();
            if (!repPath.endsWith("\\") && !repPath.endsWith("/")) {
                repPath = repPath + "/";
            }

            repPath += category + "/" + dep.formatPath() + dep.getName() + ".zip";
            stream = getInputStreamForPath(repPath);
            if (stream != null) {
                localRepositoryDependencyFolder.mkdirs();
                File localRepositoryDependencyPath = new File(localRepositoryDependencyFolder, dep.getName() + ".zip");
                try {
                    try (OutputStream outputStream = new FileOutputStream(localRepositoryDependencyPath)) {
                        IOUtils.copy(stream, outputStream);
                    }
                    unZipFile(localRepositoryDependencyPath);
                    localRepositoryDependencyPath.delete();
                    createFinishFile(localRepositoryDependencyFolder);
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot download dependency " + dep, ex);
                }
                try {
                    stream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                break;
            }
        }

        if (stream == null) {
            throw new IllegalArgumentException("Cannot find dependency [" + dep.toString() + "] in remote repositories");
        }
    }

    private void createFinishFile(File depFolder) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintStream stream = new PrintStream(new File(depFolder, "package.properties"), "UTF-8")) {
            stream.println("loaded=" + dateFormatter.format(new Date()));
        }
    }

    private void unZipFile(File zipFile) {
        byte[] buffer = new byte[1024];
        try {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    String fileName = ze.getName();
                    File newFile = new File(zipFile.getParentFile(), File.separator + fileName);
                    if (!ze.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    } else {
                        newFile.mkdirs();
                    }
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private InputStream getInputStreamForPath(String path) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("ftp://")) {
            try {
                URLConnection connection = new URL(path).openConnection();
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection httpUrlConnection = (HttpURLConnection) connection;
                    if (httpUrlConnection.getResponseCode() != 200) {
                        return null;
                    }
                    return connection.getInputStream();
                }

                return connection.getInputStream();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                //should not be throws because file.exist is done before this statement
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }
}
