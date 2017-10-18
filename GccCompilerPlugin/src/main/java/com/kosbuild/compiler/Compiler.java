package com.kosbuild.compiler;

import com.kosbuild.utils.Utils;
import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.Dependency;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.plugins.PluginConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import static com.kosbuild.compiler.CompilerUtils.fixPath;
import com.kosbuild.plugins.AbstractPlugin;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Properties;
import org.slf4j.Logger;

/**
 * @author sad
 */
public class Compiler {

    static final Logger log = Utils.getLogger();

    public Object build(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        log.info("Build project [" + buildContext.getProjectFolder().getAbsolutePath() + "]");
        File targetFolder = CompilerUtils.getTargetFolder(buildContext.getProjectFolder());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        boolean generateDebugInfo = Utils.getBooleanProperty("generateDebugInfo", pluginConfig.getConfig(), Boolean.FALSE);
        boolean hasConsole=Utils.getBooleanProperty("hasConsole", pluginConfig.getConfig(), Boolean.FALSE);
        boolean doObjCopy = Utils.getBooleanProperty("doobjcopy", pluginConfig.getConfig(), Boolean.TRUE);
        boolean stopOnFirstError = Utils.getBooleanProperty("stopOnFirstErrorFile", pluginConfig.getConfig(), Boolean.TRUE);
        String[] sourceFilesExtensions = Utils.getStringArrayProperty("sourceFileExtensions", pluginConfig.getConfig(), new String[]{"cpp", "c"});
        BinaryType resultBinaryType = CompilerUtils.getResultBinaryType(pluginConfig);
        List<String> librariesPaths = new ArrayList<>();
        List<String> librariesNames = new ArrayList<>();

        //compilation
        List<File> processedObjectFiles = new ArrayList<>();
        Set<String> seenFileNames = new HashSet<>();
        if (!compile(buildContext, pluginConfig, sourceFilesExtensions, librariesPaths, librariesNames, processedObjectFiles, seenFileNames, stopOnFirstError, generateDebugInfo)) {
            return AbstractPlugin.ERROR_RESULT;
        }

        if (resultBinaryType == BinaryType.APPLICATION) {
            log.info("Build project as executable application");
            if (!linking(pluginConfig, buildContext, librariesPaths, librariesNames)) {
                return AbstractPlugin.ERROR_RESULT;
            }
            if (doObjCopy) {
                if (!objCopyStage(pluginConfig, buildContext)) {
                    return AbstractPlugin.ERROR_RESULT;
                }
            }
            if(!hasConsole){
                changeSubsystemFlag(buildContext, pluginConfig);
            }
        } else if (resultBinaryType == BinaryType.STATIC_LIBRARY) {
            log.info("Build project as static library");
            if (!makeStaticLibrary(pluginConfig, buildContext, processedObjectFiles)) {
                return AbstractPlugin.ERROR_RESULT;
            }
        }

        return AbstractPlugin.DONE_RESULT;
    }
    
    private void changeSubsystemFlag(BuildContext buildContext, PluginConfig pluginConfig) throws FileNotFoundException, IOException{
        String applicationName=buildContext.getApplicationName();
        File outputFile=new File(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()),applicationName);
        if(!outputFile.exists()){
            throw new IllegalStateException("Output file "+outputFile.getAbsolutePath()+" does not exist. Exit");
        }
        
        RandomAccessFile raf=new RandomAccessFile(outputFile, "rw");
        raf.seek(36);
        raf.write(2);
        raf.close();
    }

    private boolean compile(BuildContext buildContext, PluginConfig pluginConfig, String[] sourceFilesExtensions, List<String> librariesPaths, List<String> librariesNames, List<File> processedObjectFiles, Set<String> seenFileNames, boolean stopOnFirstError, boolean generateDebugInfo) throws IOException {
        File targetFolder = CompilerUtils.getTargetFolder(buildContext.getProjectFolder());
        List<String> sharedArguments = createSharedCompilerArgumentsList(buildContext, pluginConfig, generateDebugInfo);
        List<File> sourceFiles = listSourceFiles(buildContext, sourceFilesExtensions);
        log.info("Compiling " + sourceFiles.size() + " source files");
        List<String> includePaths = new ArrayList<>();
        collectIncludeAndLibraryPaths(includePaths, librariesPaths, librariesNames, buildContext);
        addIncludePaths(sharedArguments, includePaths);

        boolean compilationError = false;
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
            log.error("Error while compiling project. Exit");
            return false;
        }
        return true;
    }

    private List<File> listSourceFiles(BuildContext buildContext, String[] sourceFilesExtensions) {
        File sourcesDir = CompilerUtils.getSourcesFolder(buildContext.getProjectFolder());
        if (!sourcesDir.exists()) {
            throw new IllegalStateException("Cannot find folder with sources [" + sourcesDir.getAbsolutePath() + "]");
        }
        List<File> files = new ArrayList<>();
        files.addAll(FileUtils.listFiles(sourcesDir, sourceFilesExtensions, true));
        return files;
    }

    private boolean objCopyStage(PluginConfig pluginConfig, BuildContext buildContext) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(fixPath(getNativeUtilsFolder(pluginConfig) + "objcopy"));
        args.add(buildContext.getApplicationName());
        args.add("-O");
        args.add("binary");

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        processBuilder.directory(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()));
        Process process = processBuilder.start();
        int resultCode;
        try {
            resultCode = process.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error while waiting for objcopy utility", ex);
        }
        if (resultCode != 0) {
            log.error("objcopy error. Exit");
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
            String mapFile = new File(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()), "map.map").getAbsolutePath();
            args.add(fixPath(mapFile));
        }

        for (String libraryPath : librariesPaths) {
            args.add("-L");
            args.add(fixPath(libraryPath));
        }

        args.add("-o");
        args.add(fixPath(new File(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()), buildContext.getApplicationName()).getAbsolutePath()));

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
        processBuilder.directory(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()));
        Process process = processBuilder.start();
        int resultCode;
        try {
            resultCode = process.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error while waiting for linker", ex);
        }
        if (resultCode != 0) {
            log.error("Linker error. Exit");
            return false;
        }

        return true;
    }

    private boolean makeStaticLibrary(PluginConfig pluginConfig, BuildContext buildContext, List<File> processedObjectFiles) throws IOException {
        List<String> args = new ArrayList<String>();
        args.add(fixPath(getNativeUtilsFolder(pluginConfig) + "ar"));
        args.add("rcs");
        args.add(buildContext.getApplicationName() + ".a");
        for (File processedObjectFile : processedObjectFiles) {
            args.add(fixPath(processedObjectFile.getAbsolutePath()));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        processBuilder.directory(CompilerUtils.getTargetFolder(buildContext.getProjectFolder()));
        Process process = processBuilder.start();
        int resultCode;
        try {
            resultCode = process.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error while waiting for static library archiver", ex);
        }

        if (resultCode != 0) {
            log.error("Static library archiver error. Exit");
            return false;
        }

        return true;
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

    private List<String> createSharedCompilerArgumentsList(BuildContext buildContext, PluginConfig pluginConfig, boolean generateDebugInfo) {
        List<String> args = new ArrayList<>();
        String pathToCompiler = getNativeUtilsFolder(pluginConfig) + "g++";
        args.add(pathToCompiler);
        args.add("-c");
        if (generateDebugInfo) {
            args.add("-gdwarf-4");
        }

        JsonObject conf = pluginConfig.getConfig();
        String optimizationLevel = calculateOptimizationLevel(Utils.getStringProperty("optimizationLevel", conf, "release"));
        args.add(optimizationLevel);

        String compilerSupport = parseCompilerSupport(Utils.getStringProperty("compilerSupport", conf, "c++14"));
        args.add(compilerSupport);

        File headersFolder = CompilerUtils.getHeadersFolder(buildContext);
        if (!headersFolder.exists()) {
            log.error("Headers folder [" + headersFolder + "] does not exists");
        }

        args.add("-I");
        args.add(fixPath(headersFolder.getAbsolutePath()));
        return args;
    }

    private void collectIncludeAndLibraryPaths(List<String> includePaths, List<String> libraryPaths, List<String> librariesNames, BuildContext buildContext) throws IOException {
        DependencyExtractor dependencyExtractor = new DependencyExtractor();
        for (Dependency dependency : buildContext.getDependencies()) {
            File dependencyFolder = dependencyExtractor.getPathToPackageDependencyAndLoadIfNotExists(dependency);
            processIncludeFolder(includePaths, buildContext, dependency, dependencyFolder);
            File libsFolder = new File(dependencyFolder.getAbsoluteFile(), "libs");
            libraryPaths.add(libsFolder.getAbsolutePath());
            for (File file : libsFolder.listFiles()) {
                String fileName = file.getName();
                if (fileName.toLowerCase().endsWith(".a")) {
                    if (!fileName.startsWith("lib")) {
                        log.warn("Library [" + fileName + "] in dependency [" + dependency + "] should start with 'lib'. Skip");
                        continue;
                    }

                    librariesNames.add(file.getName());
                }
            }
        }
    }

    private void processIncludeFolder(List<String> includePaths, BuildContext buildContext, Dependency dependency, File dependencyFolder) throws IOException {
        String includePath = new File(dependencyFolder.getAbsoluteFile(), "include").getAbsolutePath();

        File linkFile = new File(includePath, "includeFolder.lnk");
        if (!linkFile.exists()) {
            includePaths.add(includePath);
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

    private String getNativeUtilsFolder(PluginConfig pluginConfig) {
        return pluginConfig.getPluginLocalRepositoryFolder().getAbsolutePath() + "/nativeUtils/" + Utils.getOperationSystem() + "/bin/";
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

}