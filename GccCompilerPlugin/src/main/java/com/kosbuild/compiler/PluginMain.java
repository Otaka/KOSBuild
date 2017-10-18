package com.kosbuild.compiler;

import com.kosbuild.config.BuildContext;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.utils.Utils;
import org.slf4j.Logger;

/**
 * @author Dmitry
 */
public class PluginMain extends AbstractPlugin {

    static final Logger log = Utils.getLogger();

    @Override
    public Object call(BuildContext buildContext, PluginConfig pluginConfig, String currentStep) throws Exception {
        if (currentStep.equals(AbstractPlugin.CLEAN)) {
            return new Cleaner().clean(buildContext, pluginConfig);
        } else if (currentStep.equals(AbstractPlugin.COMPILE)) {
            return new Compiler().build(buildContext, pluginConfig);
        } else if (currentStep.equals(AbstractPlugin.INSTALL)) {
            //you can install only shared libraries
            if (CompilerUtils.getResultBinaryType(pluginConfig) != BinaryType.STATIC_LIBRARY) {
                log.debug("You can make 'INSTALL' only on static library projects");
                return true;
            } else {
                return new Installer().installStaticLibrary(buildContext, pluginConfig);
            }
        }

        throw new IllegalArgumentException(name() + ":" + version() + " plugin can be runned only on Clean and Compile and Install steps");
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
        return new String[]{AbstractPlugin.CLEAN, AbstractPlugin.COMPILE, AbstractPlugin.INSTALL};
    }

    @Override
    public String toString() {
        return name() + ":" + version();
    }
}
