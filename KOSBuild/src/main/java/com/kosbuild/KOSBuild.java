package com.kosbuild;

import com.kosbuild.utils.Utils;
import com.kosbuild.config.BuildArguments;
import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.config.Config;
import com.kosbuild.config.CrossModuleProperties;
import com.kosbuild.jsonparser.FieldValuePair;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.jsonparser.JsonParseException;
import com.kosbuild.jsonparser.JsonParser;
import com.kosbuild.jsonparser.JsonString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;

/**
 * @author Dmitry
 */
public class KOSBuild {

    static final Logger log = Utils.getLogger();
    public static final String buildFileName = "kosbuild.json";
    protected static String buildFileExtension = "json";
    private static final Pattern replaceholderMatcher = Pattern.compile("\\$\\{(.*?)\\}", Pattern.MULTILINE);

    /**
     * Run the build. Assume that "buildFile" is located in current directory
     */
    public void run(String[] args) throws FileNotFoundException, IOException {
        BuildArguments buildArguments = parseCommandLineArguments(args);
        loadConfig();
        configureLogger(buildArguments);
        for (File buildFile : buildArguments.getBuildFiles()) {
            runBuildFile(buildFile, buildArguments.getStepsToExecute().toArray(new String[0]), new CrossModuleProperties());
        }
    }

    private void configureLogger(BuildArguments buildArguments) {
        Utils.changeLogLevel(buildArguments.getLogLevel());
    }

    private BuildArguments parseCommandLineArguments(String[] args) {
        Stack<String> argsStack = new Stack<>();
        for (int i = args.length - 1; i >= 0; i--) {
            argsStack.push(args[i]);
        }

        BuildArguments ba = new BuildArguments();
        Pattern customPropertiesPatternWithArgument = Pattern.compile("^-D(.+)=(.+)$");
        while (!argsStack.isEmpty()) {
            String arg = argsStack.pop();
            if (arg.equals("-f")) {
                String buildFilePath = argsStack.pop();
                File buildFile = new File(buildFilePath, "kosbuild.json");
                if (!buildFile.exists()) {
                    throw new IllegalArgumentException("Cannot find build file [" + buildFile.getAbsolutePath() + "]");
                }

                ba.getBuildFiles().add(buildFile);
            } else if (arg.startsWith("-D")) {
                String argName;
                String argValue;
                Matcher matcher = customPropertiesPatternWithArgument.matcher(arg);
                if (matcher.matches()) {
                    argName = matcher.group(1);
                    argValue = matcher.group(2);
                } else {
                    argName = arg.substring("-D".length());
                    argValue = "true";
                }

                ba.getCustomArguments().put(argName, argValue);
            } else if (arg.equalsIgnoreCase("-verbose")) {
                ba.setLogLevel("DEBUG");
            } else if (new LifeCycleExecutor().isLifeCycleStep(arg)) {
                ba.getStepsToExecute().add(arg);
            } else {
                throw new IllegalArgumentException("Unknown argument [" + arg + "]");
            }
        }

        if (ba.getBuildFiles().isEmpty()) {
            File buildFile = new File("kosbuild.json");
            if (!buildFile.exists()) {
                throw new IllegalArgumentException("Cannot find build file [" + buildFile.getAbsolutePath() + "]");
            }

            ba.getBuildFiles().add(buildFile);
        }

        return ba;
    }

    public void runBuildFile(File buildFile, String[] goals, CrossModuleProperties crossModuleProperties) throws IOException {
        BuildContext buildContext = new BuildContext();
        buildContext.setBuildFile(buildFile);
        buildContext.setProjectFolder(buildFile.getParentFile());
        JsonObject parsedFile = readAndParseFile(buildFile);
        if (parsedFile.contains("properties")) {
            replacePlaceholders(parsedFile, parseProperties(parsedFile.getElementByName("properties").getAsObject()));
        }
        String projectName = Utils.getStringProperty("name", parsedFile, null);
        validateProjectName(projectName, buildFile);

        String version = Utils.getStringProperty("version", parsedFile, null);
        if (version == null) {
            throw new IllegalArgumentException("Mandatory [version] property is not found in [" + buildFile.getAbsolutePath() + "] build file");
        }
        buildContext.setApplicationName(projectName);
        buildContext.setVersion(version);

        DependencyExtractor dependencyManager = new DependencyExtractor();
        dependencyManager.collectDependencies(parsedFile, buildContext, crossModuleProperties);
        dependencyManager.collectPlugins(parsedFile, buildContext);

        LifeCycleExecutor lifeCycleExecutor = new LifeCycleExecutor();
        lifeCycleExecutor.execute(buildContext, goals);
    }

    private void validateProjectName(String projectName, File buildFile) {
        if (projectName == null) {
            throw new IllegalArgumentException("Mandatory [name] property is not found in [" + buildFile.getAbsolutePath() + "] build file");
        }

        String[] badSymbols = new String[]{" ", "\\", "/", "?", "(", ")", "[", "]", ":", ";", "^", "&", "*", "+", "#", "@", "!", "%", ".", ",", "\"", "\'", "$"};
        for (String badSymbol : badSymbols) {
            if (projectName.contains(badSymbol)) {
                throw new IllegalArgumentException("Project name [" + projectName + "] in build file [" + buildFile.getAbsolutePath() + "] contains wrong symbol [" + badSymbol + "]");
            }
        }
    }

    private void loadConfig() throws FileNotFoundException {
        Config.get().load(new File(Utils.getAppFolder(), "../../configuration.json"));
    }

    private void replacePlaceholders(JsonElement json, Map<String, String> propertiesMap) {
        if (json.isObject()) {
            for (FieldValuePair fvp : json.getAsObject().getElements()) {
                fvp.setName(replacePlaceholder(fvp.getName(), propertiesMap));
                replacePlaceholders(fvp.getValue(), propertiesMap);
            }
        } else if (json.isArray()) {
            JsonArray array = json.getAsArray();
            for (JsonElement element : array.getElements()) {
                replacePlaceholders(element, propertiesMap);
            }
        } else if (json.isPrimitive()) {
            if (json instanceof JsonString) {
                JsonString str = (JsonString) json;
                str.setValue(replacePlaceholder(str.getAsString(), propertiesMap));
            }
        }
    }

    private String replacePlaceholder(String value, Map<String, String> propertiesMap) {
        if (!value.contains("$")) {
            return value;
        }

        Matcher m = replaceholderMatcher.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String property = m.group(1).toLowerCase();
            String propertyValue = propertiesMap.get(property);
            if (propertyValue == null) {
                throw new IllegalArgumentException("Cannot find property [" + property + "]");
            }

            m.appendReplacement(sb, propertyValue);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private Map<String, String> parseProperties(JsonObject properties) {
        Map<String, String> result = new HashMap<>();
        for (FieldValuePair fvp : properties.getElements()) {
            result.put(fvp.getName().toLowerCase(), fvp.getValue().getAsString());
        }

        return result;
    }

    protected JsonObject readAndParseFile(File buildFile) {
        JsonParser parser = new JsonParser();
        JsonElement parsedFile;
        try {
            parsedFile = parser.parse(new InputStreamReader(new BOMInputStream(new FileInputStream(buildFile)), "UTF-8"));
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find file " + buildFile.getAbsolutePath(), ex);
        } catch (JsonParseException ex) {
            throw new IllegalArgumentException("Cannot parse file " + buildFile.getAbsolutePath(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot parse file " + buildFile.getAbsolutePath(), ex);
        }

        if (!parsedFile.isObject()) {
            throw new IllegalArgumentException("Build file " + buildFile.getAbsolutePath() + " in wrong format. Root should be json object.");
        }

        JsonObject parsedFileObject = parsedFile.getAsObject();
        if (parsedFileObject.contains("parent")) {
            String parentPath = parsedFileObject.removeElementByName("parent").getAsString();
            JsonObject parentParsedFileObject = readParent(parentPath, buildFile);
            checkParentJsonObjectDoNotContainPlusMinusFields(parentParsedFileObject, parentPath);
            parsedFileObject = new BuildFileMerger().mergeParsedBuildFiles(parentParsedFileObject, parsedFileObject);
        }
        //parsedFileObject.getAsString();
        return parsedFileObject;
    }

    private void checkParentJsonObjectDoNotContainPlusMinusFields(JsonObject jsonObject, String parentPath) {
        for (FieldValuePair fvp : jsonObject.getElements()) {
            if (fvp.getName().startsWith("+") || fvp.getName().startsWith("-") || fvp.getName().startsWith("~")) {
                throw new IllegalArgumentException("Root build files should not contain fields started with + or - or ~. Field [" + fvp.getName() + "] in [" + parentPath + "]");
            }
            if (fvp.getValue().isObject()) {
                checkParentJsonObjectDoNotContainPlusMinusFields(fvp.getValue().getAsObject(), parentPath);
            }
        }
    }

    private JsonObject readParent(String parentString, File buildFile) {
        File parent = new File(buildFile.getParentFile(), parentString + "." + buildFileExtension);
        if (!parent.exists()) {
            throw new IllegalArgumentException("Cannot find parent file " + parent.getAbsolutePath() + " defined in buildFile " + buildFile.getAbsolutePath());
        }

        return readAndParseFile(parent);
    }
}
