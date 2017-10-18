package com.kosbuild.plugins;

import com.kosbuild.config.BuildContext;
import com.kosbuild.jsonparser.JsonObject;
import java.io.File;

/**
 * @author Dmitry
 */
public class PluginConfig {

    private String name;
    private String version;
    private String[] overrideRunOnSteps;
    private JsonObject config;
    private AbstractPlugin dynamicPluginObject;
    private File pluginLocalRepositoryFolder;
    

    public boolean isStepsOverriden() {
        return overrideRunOnSteps != null;
    }

    public File getPluginLocalRepositoryFolder() {
        return pluginLocalRepositoryFolder;
    }

    public void setPluginLocalRepositoryFolder(File pluginLocalRepositoryFolder) {
        this.pluginLocalRepositoryFolder = pluginLocalRepositoryFolder;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return config;
    }

    public AbstractPlugin getDynamicPluginObject() {
        return dynamicPluginObject;
    }

    public void setDynamicPluginObject(AbstractPlugin dynamicPluginObject) {
        this.dynamicPluginObject = dynamicPluginObject;
    }

    public void setOverrideRunOnSteps(String[] overrideRunOnSteps) {
        this.overrideRunOnSteps = overrideRunOnSteps;
    }

    public String[] getOverrideRunOnSteps() {
        return overrideRunOnSteps;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return ""+name+":"+version;
    }
    
    public Object call(BuildContext buildContext, String currentStep) throws Exception{
        return dynamicPluginObject.call(buildContext, this, currentStep);
    }

    public Object call(BuildContext buildContext) throws Exception{
        return dynamicPluginObject.call(buildContext, this, "NO_STEP");
    }
}
