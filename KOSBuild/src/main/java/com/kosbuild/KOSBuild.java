package com.kosbuild;

import com.kosbuild.config.BuildContext;
import com.kosbuild.dependencies.DependencyExtractor;
import com.kosbuild.config.Config;
import com.kosbuild.jsonparser.FieldValuePair;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.jsonparser.JsonParseException;
import com.kosbuild.jsonparser.JsonParser;
import com.kosbuild.jsonparser.JsonString;
import com.kosbuild.plugins.AbstractPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.BOMInputStream;

/**
 * @author Dmitry
 */
public class KOSBuild {

    public static final String buildFileName = "kosbuild.json";
    protected static String buildFileExtension = "json";

    /**
     * Run the build. Assume that "buildFile" is located in current directory
     */
    public void run() throws FileNotFoundException, IOException {
        loadConfig();
        File buildFile = new File("./example/" + buildFileName);
        if (!buildFile.exists()) {
            throw new IllegalArgumentException("Cannot find file " + buildFileName + " in current working directory " + new File(".").getAbsolutePath());
        }

        runBuildFile(buildFile, new String[]{AbstractPlugin.CLEAN, AbstractPlugin.COMPILE});
    }

    public void runBuildFile(File buildFile, String[] goals) throws IOException {
        BuildContext buildContext = new BuildContext();
        buildContext.setBuildFile(buildFile);
        buildContext.setProjectFolder(buildFile.getParentFile());
        JsonObject parsedFile = readAndParseFile(buildFile);
        if (parsedFile.contains("properties")) {
            replacePlaceholders(parsedFile, parseProperties(parsedFile.getElementByName("properties").getAsObject()));
        }
        String projectName = Utils.getStringProperty("name", parsedFile, null);
        if (projectName == null) {
            throw new IllegalArgumentException("Mandatory [name] property is not found in [" + buildFile.getAbsolutePath() + "] build file");
        }

        String version = Utils.getStringProperty("version", parsedFile, null);
        if (version == null) {
            throw new IllegalArgumentException("Mandatory [version] property is not found in [" + buildFile.getAbsolutePath() + "] build file");
        }
        buildContext.setApplicationName(projectName);
        buildContext.setVersion(version);

        DependencyExtractor dependencyManager = new DependencyExtractor();
        dependencyManager.collectDependencies(parsedFile, buildContext);
        dependencyManager.collectPlugins(parsedFile, buildContext);

        LifeCycleExecutor lifeCycleExecutor = new LifeCycleExecutor();
        lifeCycleExecutor.execute(buildContext, goals);
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

    private static final Pattern replaceholderMatcher = Pattern.compile("\\$\\{(.*?)\\}", Pattern.MULTILINE);

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
