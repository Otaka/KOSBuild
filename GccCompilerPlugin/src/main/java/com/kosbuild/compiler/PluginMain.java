package com.kosbuild.compiler;

import com.kosbuild.Utils;
import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.Dependency;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Dmitry
 */
public class PluginMain extends AbstractPlugin {

    @Override
    public boolean call(BuildContext buildContext, PluginConfig pluginConfig, String currentStep) throws Exception {
        if (currentStep.equals(AbstractPlugin.CLEAN)) {
            return clean(buildContext, pluginConfig);
        } else if (currentStep.equals(AbstractPlugin.COMPILE)) {
            return build(buildContext, pluginConfig);
        }
        throw new IllegalArgumentException(name() + ":" + version() + " plugin can be runned only on Clean and Compile steps");
    }

    public boolean clean(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        File targetFolder = getTargetFolder(buildContext.getProjectFolder());
        System.out.println("Clean project [" + buildContext.getProjectFolder().getAbsolutePath() + "]");
        if (targetFolder.exists()) {
            System.out.println("Remove target folder [" + targetFolder.getAbsolutePath() + "]");
            try {
                FileUtils.deleteDirectory(targetFolder);
                System.out.println("Removed");
            } catch (Exception ex) {
                System.err.println("Error while removing directory [" + targetFolder + "]. " + ex.getMessage());
                return false;
            }
        } else {
            System.out.println("Nothing to clear");
        }

        return true;
    }

    public boolean build(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        System.out.println("Build project [" + buildContext.getProjectFolder().getAbsolutePath() + "]");
        return compile(buildContext, pluginConfig);
    }

    private List<File> listSourceFiles(BuildContext buildContext, String[] sourceFilesExtensions) {
        File sourcesDir = getSourcesFolder(buildContext.getProjectFolder());
        if (!sourcesDir.exists()) {
            throw new IllegalStateException("Cannot find folder with sources [" + sourcesDir.getAbsolutePath() + "]");
        }
        List<File> files = new ArrayList<>();
        files.addAll(FileUtils.listFiles(sourcesDir, sourceFilesExtensions, true));
        return files;
    }

    private boolean compile(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        List<String> sharedArguments = createSharedCompilerArgumentsList(buildContext, pluginConfig);
        List<String> includePaths = new ArrayList<>();
        List<String> librariesPaths = new ArrayList<>();
        List<String> librariesNames = new ArrayList<>();
        collectIncludeAndLibraryPaths(includePaths, librariesPaths, librariesNames, buildContext);
        addIncludePaths(sharedArguments, includePaths);

        boolean stopOnFirstError = Utils.getBooleanProperty("stopOnFirstErrorFile", pluginConfig.getConfig(), Boolean.TRUE);
        String[] sourceFilesExtensions = Utils.getStringArrayProperty("sourceFileExtensions", pluginConfig.getConfig(), new String[]{"cpp", "c"});
        List<File> sourceFiles = listSourceFiles(buildContext, sourceFilesExtensions);
        boolean compilationError = false;
        File targetFolder = getTargetFolder(buildContext.getProjectFolder());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        //compilation
        List<File> processedObjectFiles = new ArrayList<>();
        Set<String> seenFileNames = new HashSet<>();
        for (File sourceFile : sourceFiles) {
            List<String> args = new ArrayList<>(sharedArguments);

            File objectFile = chooseNewFileNameToObjectFile(sourceFile, targetFolder, seenFileNames);
            processedObjectFiles.add(objectFile);
            args.add("-o");
            args.add(fixPath(objectFile.getAbsolutePath()));

            args.add(fixPath(sourceFile.getAbsolutePath()));

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.inheritIO();
            processBuilder.directory(buildContext.getProjectFolder());
            Process process = processBuilder.start();
            int resultCode;
            try {
                resultCode = process.waitFor();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Error while waiting for compiler", ex);
            }

            if (resultCode != 0) {
                compilationError = true;
                if (stopOnFirstError) {
                    break;
                }
            }
        }

        if (compilationError) {
            System.out.println("Error while compiling project. Exit");
            return false;
        }

        if (!linking(pluginConfig, buildContext, librariesPaths, librariesNames)) {
            return false;
        }

        if (!objCopyStage(pluginConfig, buildContext)) {
            return false;
        }

        return true;
    }

    private boolean objCopyStage(PluginConfig pluginConfig, BuildContext buildContext) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(fixPath(getNativeUtilsFolder(pluginConfig) + "objcopy"));
        args.add(buildContext.getApplicationName());
        args.add("-O");
        args.add("binary");

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        processBuilder.directory(getTargetFolder(buildContext.getProjectFolder()));
        Process process = processBuilder.start();
        int resultCode;
        try {
            resultCode = process.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error while waiting for compiler", ex);
        }
        if (resultCode != 0) {
            System.err.println("Linker error. Exit");
            return false;
        }

        return true;
    }

    private boolean linking(PluginConfig pluginConfig, BuildContext buildContext, List<String> librariesPaths, List<String> librariesNames) throws IOException {
        boolean generateMapFile = false;
        if (pluginConfig.getConfig().contains("generateMapFile")) {
            generateMapFile = pluginConfig.getConfig().getElementByName("generateMapFile").getAsBoolean();
        }
        List<String> args = new ArrayList<>();
        args.add(fixPath(getNativeUtilsFolder(pluginConfig) + "ld"));

        args.add("-static");
        args.add("-S");
        args.add("-nostdlib");

        args.add("-T" + fixPath(pluginConfig.getPluginLocalRepositoryFolder().getAbsolutePath() + "/linkerScripts/app.lds"));

        args.add("--image-base");
        args.add("0");

        if (generateMapFile) {
            args.add("-Map");
            String mapFile = new File(getTargetFolder(buildContext.getProjectFolder()), "map.map").getAbsolutePath();
            args.add(fixPath(mapFile));
        }

        for (String libraryPath : librariesPaths) {
            args.add("-L");
            args.add(fixPath(libraryPath));
        }

        args.add("-o");
        args.add(fixPath(new File(getTargetFolder(buildContext.getProjectFolder()), buildContext.getApplicationName()).getAbsolutePath()));

        args.add("*.o");

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < librariesNames.size(); i++) {
                String library = librariesNames.get(i);
                String libraryFileName = FilenameUtils.getBaseName(library);
                if (libraryFileName.startsWith("lib")) {
                    libraryFileName = libraryFileName.substring(3);
                }
                args.add("-l" + libraryFileName);
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        processBuilder.directory(getTargetFolder(buildContext.getProjectFolder()));
        Process process = processBuilder.start();
        int resultCode;
        try {
            resultCode = process.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error while waiting for compiler", ex);
        }
        if (resultCode != 0) {
            System.err.println("Linker error. Exit");
            return false;
        }

        return true;
    }

    private String fixPath(String val) {
        return val.replace("\\", "/").replace("/./", "/");
    }

    private File chooseNewFileNameToObjectFile(File sourceFile, File objectFilesOutputFolder, Set<String> seenFileNames) {
        String fileName = FilenameUtils.getBaseName(sourceFile.getName());
        String fileNameLowerCase = fileName.toLowerCase();
        for (int i = 0; i < 10000; i++) {
            String newFileNameLower;
            String newFileName;

            if (i == 0) {
                newFileNameLower = fileNameLowerCase;
                newFileName = fileName;
            } else {
                newFileNameLower = fileNameLowerCase + i;
                newFileName = fileName + i;
            }

            if (!seenFileNames.contains(newFileNameLower)) {
                seenFileNames.add(newFileNameLower);
                return new File(objectFilesOutputFolder, newFileName + ".o");
            }
        }

        throw new RuntimeException("Cannot choose new new for object file [" + sourceFile.getAbsolutePath() + "]");
    }

    private void addIncludePaths(List<String> args, List<String> includePaths) {
        for (String includePath : includePaths) {
            args.add("-I");
            args.add(fixPath(includePath));
        }
    }

    private String getNativeUtilsFolder(PluginConfig pluginConfig) {
        return pluginConfig.getPluginLocalRepositoryFolder().getAbsolutePath() + "/nativeUtils/" + Utils.getOperationSystem() + "/bin/";
    }

    private List<String> createSharedCompilerArgumentsList(BuildContext buildContext, PluginConfig pluginConfig) {
        List<String> args = new ArrayList<>();
        String pathToCompiler = getNativeUtilsFolder(pluginConfig) + "g++";
        args.add(pathToCompiler);
        args.add("-c");

        JsonObject conf = pluginConfig.getConfig();
        String optimizationLevel = calculateOptimizationLevel(Utils.getStringProperty("optimizationLevel", conf, "release"));
        args.add(optimizationLevel);

        String compilerSupport = parseCompilerSupport(Utils.getStringProperty("compilerSupport", conf, "c++14"));
        args.add(compilerSupport);

        File headersFolder = getHeadersFolder(buildContext.getProjectFolder());
        if (!headersFolder.exists()) {
            System.err.println("Headers folder [" + headersFolder + "] does not exists");
        }
        args.add("-I");
        args.add(fixPath(headersFolder.getAbsolutePath()));
        return args;
    }

    private void collectIncludeAndLibraryPaths(List<String> includePaths, List<String> libraryPaths, List<String> librariesNames, BuildContext buildContext) {
        DependencyExtractor dependencyExtractor = new DependencyExtractor();
        for (Dependency dependency : buildContext.getDependencies()) {
            File dependencyFolder = dependencyExtractor.getPathToPackageDependencyAndLoadIfNotExists(dependency);
            String includePath = new File(dependencyFolder.getAbsoluteFile(), "include").getAbsolutePath();
            if (!dependency.isIncludeWithoutPrefix()) {
                includePath = new File(includePath, dependency.getName()).getAbsolutePath();
            }
            includePaths.add(includePath);
            File libsFolder = new File(dependencyFolder.getAbsoluteFile(), "libs");
            libraryPaths.add(libsFolder.getAbsolutePath());
            for (File file : libsFolder.listFiles()) {
                String fileName = file.getName();
                if (fileName.toLowerCase().endsWith(".a")) {
                    if (!fileName.startsWith("lib")) {
                        System.out.println("Library [" + fileName + "] in dependency [" + dependency + "] should start with 'lib'. Skip");
                        continue;
                    }
                    librariesNames.add(file.getName());
                }
            }
        }
    }

    private String calculateOptimizationLevel(String optimizationConfig) {
        switch (optimizationConfig.toLowerCase()) {
            case "debug":
                return "-Og";
            case "release":
                return "-O1";
            case "releasefast":
                return "-O2";
            case "releasefastest":
                return "-O3";
            default:
                throw new IllegalArgumentException("Do not understand optimization flag [" + optimizationConfig + "]. Supported only [debug, release, releaseFast, releaseFastest]");
        }
    }

    private String parseCompilerSupport(String compilerSupport) {
        switch (compilerSupport.toLowerCase()) {
            case "c++11":
                return "-std=c++11";
            case "c++14":
                return "-std=c++14";
            case "c++17":
                return "-std=c++17";
            case "c++old":
                return "";
            default:
                throw new IllegalArgumentException("Do not understand compilerSupport flag [" + compilerSupport + "]. Supported only [c++11, c++14, c++17, c++old]");
        }
    }

    private File getTargetFolder(File projectFolder) {
        return new File(projectFolder, "target");
    }

    /**
     * Folder where headers and sources folders are stored
     */
    private File getSrcFolder(File projectFolder) {
        return new File(projectFolder, "src");
    }

    /**
     * Folder inside src folder. It stores source files
     */
    private File getSourcesFolder(File projectFolder) {
        return new File(getSrcFolder(projectFolder), "sources");
    }

    /**
     * Folder inside src folder. It stores header files
     */
    private File getHeadersFolder(File projectFolder) {
        return new File(getSrcFolder(projectFolder), "headers");
    }

    @Override
    public String name() {
        return "gcc";
    }

    @Override
    public String version() {
        return "5.4.0";
    }

    @Override
    public String[] getStages() {
        return new String[]{AbstractPlugin.CLEAN, AbstractPlugin.COMPILE};
    }
}
