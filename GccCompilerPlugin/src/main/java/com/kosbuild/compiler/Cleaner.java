package com.kosbuild.compiler;

import com.kosbuild.config.BuildContext;
import com.kosbuild.plugins.AbstractPlugin;
import com.kosbuild.plugins.PluginConfig;
import com.kosbuild.utils.Utils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 * @author sad
 */
public class Cleaner {

    static final Logger log = Utils.getLogger();

    public Object clean(BuildContext buildContext, PluginConfig pluginConfig) throws IOException {
        File targetFolder = CompilerUtils.getTargetFolder(buildContext.getProjectFolder());
        log.info("Clean project [" + buildContext.getProjectFolder().getAbsolutePath() + "]");
        if (targetFolder.exists()) {
            log.info("Remove target folder [" + targetFolder.getAbsolutePath() + "]");

            try {
                FileUtils.cleanDirectory(targetFolder);
            } catch (Exception ex) {
                log.error("Error while cleaning target folder [" + targetFolder + "]. ", ex);
                return AbstractPlugin.ERROR_RESULT;
            }

            try {
                FileUtils.deleteDirectory(targetFolder);
            } catch (Exception ex) {
                log.warn("Cannot remove target folder [" + targetFolder + "]. ");
                log.debug("Cannot remove target folder [" + targetFolder + "]. ", ex);

            }
        } else {
            log.debug("Nothing to clear");
        }

        log.debug("Cleared target folder");
        return AbstractPlugin.DONE_RESULT;
    }
}
