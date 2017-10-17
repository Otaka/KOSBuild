package com.kosbuild;

import com.kosbuild.config.BuildContext;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * @author Dmitry
 */
public class LifeCycleExecutor {

    static final Logger log = Utils.getLogger();
    
    public static String[] cleanStage = new String[]{
        AbstractPlugin.CLEAN
    };
    public static String[] buildStage = new String[]{
        AbstractPlugin.VALIDATE, AbstractPlugin.COMPILE_SUBMODULE, AbstractPlugin.COMPILE, AbstractPlugin.TEST,
        AbstractPlugin.INSTALL
    };

    public static String[] deployLocal = new String[]{
        AbstractPlugin.DEPLOY_LOCAL
    };
    public static String[] deployRemote = new String[]{
        AbstractPlugin.DEPLOY_REMOTE
    };

    public boolean isLifeCycleStep(String value) {
        if (arrayIndexOfIgnoreCase(cleanStage, value) >= 0) {
            return true;
        }

        if (arrayIndexOfIgnoreCase(buildStage, value) >= 0) {
            return true;
        }
        if (arrayIndexOfIgnoreCase(deployLocal, value) >= 0) {
            return true;
        }
        if (arrayIndexOfIgnoreCase(deployRemote, value) >= 0) {
            return true;
        }

        return false;
    }

    public void execute(BuildContext buildContext, String[] goalsToExecute) {
        String[] fullStepsListToExecute = obtainFullStepsList(goalsToExecute);
        for (String currentStep : fullStepsListToExecute) {
            runPlugins(buildContext, currentStep);
        }
    }

    private void runPlugins(BuildContext buildContext, String currentStep) {
        boolean logWritten = false;
        for (PluginConfig plugin : buildContext.getPlugins()) {
            String[] runOnStages;
            if (plugin.isStepsOverriden()) {
                runOnStages = plugin.getOverrideRunOnSteps();
            } else {
                runOnStages = plugin.getDynamicPluginObject().getStages();
            }

            if (arrayIndexOfIgnoreCase(runOnStages, currentStep) >= 0) {
                if (logWritten == false) {
                    log.info("Run step [" + currentStep + "]");
                    logWritten = true;
                }

                AbstractPlugin pluginObject = plugin.getDynamicPluginObject();
                try {
                    if(!pluginObject.call(buildContext, plugin, currentStep)){
                        throw new RuntimeException("Error while execute plugin [" + pluginObject + "] on step [" + currentStep + "]");
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Error while execute plugin [" + pluginObject + "] on step [" + currentStep + "]", ex);
                }
            }
        }
    }

    /**
     * Methods obtains full list of actions that should be executed For example
     * if user wants to execute "compile", in this case VALIDATE and
     * COMPILE_SUBMODULE will be executed automatically
     */
    protected String[] obtainFullStepsList(String[] goals) {
        List<String> result = new ArrayList<>();
        for (String goal : goals) {
            int index = arrayIndexOfIgnoreCase(cleanStage, goal);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    if (!result.contains(cleanStage[i])) {
                        result.add(cleanStage[i]);
                    }
                }
            }

            index = arrayIndexOfIgnoreCase(buildStage, goal);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    if (!result.contains(buildStage[i])) {
                        result.add(buildStage[i]);
                    }
                }
            }

            index = arrayIndexOfIgnoreCase(deployLocal, goal);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    if (!result.contains(deployLocal[i])) {
                        result.add(deployLocal[i]);
                    }
                }
            }

            index = arrayIndexOfIgnoreCase(deployRemote, goal);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    if (!result.contains(deployRemote[i])) {
                        result.add(deployRemote[i]);
                    }
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    public static int arrayIndexOfIgnoreCase(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            String arrayElement = array[i];
            if (value.equalsIgnoreCase(arrayElement)) {
                return i;
            }
        }

        return -1;
    }
}
