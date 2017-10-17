package com.kosbuild.compiler;

import com.kosbuild.config.BuildContext;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.plugins.PluginConfig;
import java.io.File;
import java.util.Arrays;

/**
 * @author sad
 */
public class CompilerUtils {

    public static BinaryType getResultBinaryType(PluginConfig pluginConfig) {
        JsonElement resultBinaryTypeJsonElement = pluginConfig.getConfig().getElementByName("resultBinaryType");
        if (resultBinaryTypeJsonElement == null) {
            throw new IllegalArgumentException("Missing [resultBinaryType] property in [" + pluginConfig.getName() + ":" + pluginConfig.getVersion() + "] compiler configuration");
        }

        String resultBinaryTypeString = resultBinaryTypeJsonElement.getAsString().toUpperCase();
        try {
            return BinaryType.valueOf(resultBinaryTypeString);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown value of 'resultBinaryType' property [" + resultBinaryTypeString + "]. Possible values: " + Arrays.deepToString(BinaryType.values()));
        }
    }

    public static File getTargetFolder(File projectFolder) {
        return new File(projectFolder, "target");
    }

    /**
     * Folder inside src folder. It stores header files
     */
    public static File getHeadersFolder(BuildContext buildContext) {
        return new File(getSrcFolder(buildContext.getProjectFolder()), "headers");
    }

    /**
     * Folder inside src folder. It stores source files
     */
    public static File getSourcesFolder(File projectFolder) {
        return new File(getSrcFolder(projectFolder), "sources");
    }

    /**
     * Folder where headers and sources folders are stored
     */
    public static File getSrcFolder(File projectFolder) {
        return new File(projectFolder, "src");
    }
    
    public static String fixPath(String val) {
        return val.replace("\\", "/").replace("/./", "/");
    }
}
