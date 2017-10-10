package com.kosbuild.dependencies;

import com.kosbuild.LifeCycleExecutor;
import com.kosbuild.Utils;
import com.kosbuild.config.BuildContext;
import com.kosbuild.config.Config;
import com.kosbuild.config.CrossModuleProperties;
import com.kosbuild.config.Repository;
import com.kosbuild.jsonparser.FieldValuePair;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.jsonparser.JsonParseException;
import com.kosbuild.jsonparser.JsonParser;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.plugins.PluginManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

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

    public void collectDependencies(JsonObject jsonObject, BuildContext buildContext, CrossModuleProperties crossModuleProperties) {
        if (jsonObject.contains("dependencies")) {
            if (!jsonObject.getElementByName("dependencies").isArray()) {
                throw new IllegalArgumentException("[dependencies] section should be array of objects, but found [" + jsonObject.getElementByName("dependencies").getClass().getName() + "]");
            }

            JsonArray dependencies = jsonObject.getElementByName("dependencies").getAsArray();
            Set<Dependency> dependenciesForTransientSearch = new HashSet<Dependency>();
            for (JsonElement element : dependencies.getElements()) {
                Dependency dependency = convertJsonObjectToDependencyObject(element.getAsObject(), buildContext, crossModuleProperties);
                if (dependency.isTransitive()) {
                    dependenciesForTransientSearch.add(dependency);
                }

                buildContext.addDependency(dependency);
            }

            for (Dependency dependency : buildContext.getDependencies()) {
                downloadIfNotExists(dependency, "packages");
            }

            processTransitiveDependencies(buildContext, crossModuleProperties, dependenciesForTransientSearch);
        }
    }

    private Dependency convertJsonObjectToDependencyObject(JsonObject jsonObject, BuildContext buildContext, CrossModuleProperties crossModuleProperties) {
        Dependency dependency = new Dependency();
        dependency.setOwnerProject(buildContext.getApplicationName() + ":" + buildContext.getVersion());
        String compiler = jsonObject.getElementByName("compiler").getAsString();
        String nameAndVersion = jsonObject.getElementByName("name").getAsString();
        boolean transitive = Utils.getBooleanProperty("transitive", jsonObject, Boolean.FALSE);

        boolean includeWithoutPrefix = Utils.getBooleanProperty("includeWithoutPrefix", jsonObject, Boolean.FALSE);

        if (!nameAndVersion.contains(":")) {
            throw new IllegalArgumentException("Dependency name [" + nameAndVersion + "] is in wrong format. Proper format \"NAME:VERSION\"");
        }

        String name = nameAndVersion.substring(0, nameAndVersion.indexOf(':'));
        String version = nameAndVersion.substring(nameAndVersion.indexOf(':') + 1);
        dependency.setCompiler(compiler);
        dependency.setName(name);
        dependency.setVersion(version);
        if (transitive) {
            dependency.setTransitive(true);
            if (isDependencyIsSuppresedForTransientChildDependencies(crossModuleProperties, dependency)) {
                dependency.setTransitive(false);
            }
        } else {
            dependency.setTransitive(false);
        }

        dependency.setIncludeWithoutPrefix(includeWithoutPrefix);
        return dependency;
    }

    private void processTransitiveDependencies(BuildContext buildContext, CrossModuleProperties crossModuleProperties, Set<Dependency> dependenciesForTransientSearch) {
        while (!dependenciesForTransientSearch.isEmpty()) {
            Iterator<Dependency> iterator = dependenciesForTransientSearch.iterator();
            Dependency dependency = iterator.next();
            iterator.remove();
            if (!dependency.isTransitive()) {
                continue;
            }

            JsonArray transitiveDependenciesJsonForDependency = getTransitiveDependenciesJsonFromDependency(dependency);
            for (JsonElement transitiveDependencyJson : transitiveDependenciesJsonForDependency.getElements()) {
                Dependency transitiveDependency = convertJsonObjectToDependencyObject(transitiveDependencyJson.getAsObject(), buildContext, crossModuleProperties);
                if(!buildContext.getDependencies().contains(transitiveDependency)){
                    buildContext.addDependency(dependency);
                    if(transitiveDependency.isTransitive()){
                        dependenciesForTransientSearch.add(transitiveDependency);
                    }
                }
            }
        }
    }

    private JsonArray getTransitiveDependenciesJsonFromDependency(Dependency dependency) {
        File dependencyFolder = getPathToPackageDependencyAndLoadIfNotExists(dependency);
        File transientDependencyFileForDependency = new File(dependencyFolder, "transitiveDependencies.json");
        if (!transientDependencyFileForDependency.exists()) {
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonElement parsedFile;
        try {
            parsedFile = parser.parse(new InputStreamReader(new BOMInputStream(new FileInputStream(transientDependencyFileForDependency)), "UTF-8"));
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find file " + transientDependencyFileForDependency.getAbsolutePath(), ex);
        } catch (JsonParseException ex) {
            throw new IllegalArgumentException("Cannot parse file " + transientDependencyFileForDependency.getAbsolutePath(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot parse file " + transientDependencyFileForDependency.getAbsolutePath(), ex);
        }

        if (!parsedFile.isObject()) {
            throw new IllegalArgumentException("Build file " + transientDependencyFileForDependency.getAbsolutePath() + " in wrong format. Root should be json object.");
        }

        JsonObject parsedFileObject = parsedFile.getAsObject();
        if (parsedFileObject.contains("dependencies")) {
            return parsedFileObject.getElementByName("dependencies").getAsArray();
        }

        return null;
    }

    private boolean isDependencyIsSuppresedForTransientChildDependencies(CrossModuleProperties crossModuleProperties, Dependency dep) {
        for (Dependency suppresedDependency : crossModuleProperties.getListOfDependenciesWithDisabledTransient()) {
            if (suppresedDependency.equals(dep)) {
                return true;
            }
        }

        return false;
    }

    public File getPathToPluginDependencyAndLoadIfNotExists(Dependency dep) {
        if (!isDependencyExistsInLocalRepository(dep, "plugins")) {
            downloadIfNotExists(dep, "plugins");
        }

        return getPathToDependencyInLocalRepository(dep, "plugins");
    }

    public File getPathToPackageDependencyAndLoadIfNotExists(Dependency dep) {
        if (!isDependencyExistsInLocalRepository(dep, "packages")) {
            downloadIfNotExists(dep, "packages");
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

    private void downloadIfNotExists(Dependency dependency, String category) {
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
