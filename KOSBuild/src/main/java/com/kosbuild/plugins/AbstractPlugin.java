package com.kosbuild.plugins;

import com.kosbuild.config.BuildContext;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;

/**
 * @author Dmitry
 */
public abstract class AbstractPlugin {

    public static final String CLEAN = "clean";

    public static final String VALIDATE = "validate";
    public static final String COMPILE_SUBMODULE = "compile_submodule";
    public static final String COMPILE = "compile";
    public static final String TEST = "test";
    public static final String PACKAGE = "package";
    public static final String VERIFY = "verify";
    public static final String INSTALL = "install";
    public static final String DEPLOY_LOCAL = "deploylocal";
    public static final String DEPLOY_REMOTE = "deployremote";

    public abstract String name();

    public abstract String version();

    public abstract boolean call(BuildContext buildContext, PluginConfig pluginConfig, String currentStep) throws Exception;

    public void init() {
    }

    ;

    public abstract String[] getStages();

    
}
