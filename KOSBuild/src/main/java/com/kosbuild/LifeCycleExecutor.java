package com.kosbuild;

import com.kosbuild.config.BuildContext;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class LifeCycleExecutor {

    public static String[] cleanStage = new String[]{
        AbstractPlugin.CLEAN
    };
    public static String[] buildStage = new String[]{
        AbstractPlugin.VALIDATE, AbstractPlugin.COMPILE_SUBMODULE, AbstractPlugin.COMPILE, AbstractPlugin.TEST,
        AbstractPlugin.PACKAGE, AbstractPlugin.VERIFY, AbstractPlugin.INSTALL, AbstractPlugin.DEPLOY
    };

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
                    System.out.println("Run step [" + currentStep + "]");
                    logWritten = true;
                }

                AbstractPlugin pluginObject = plugin.getDynamicPluginObject();
                try {
                    pluginObject.call(buildContext, plugin, currentStep);
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
