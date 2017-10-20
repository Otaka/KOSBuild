package org.visualeagle.project.projectloaders;

import com.kosbuild.KOSBuild;
import com.kosbuild.config.Config;
import com.kosbuild.config.RunPluginCommandLine;
import com.kosbuild.plugins.PluginResults;
import com.kosbuild.utils.Utils;
import java.io.File;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.project.vnodes.VirtualFileVNode;
import org.visualeagle.utils.ImageManager;

/**
 * @author sad
 */
public class KosBuildGccProjectLoader extends ProjectLoader {

    @Override
    public ProjectStructure loadProject(File folder) throws Exception {
        if (!folder.exists()) {
            throw new IllegalArgumentException("Project folder [" + folder.getAbsolutePath() + "] is not exists");
        }

        File buildFile = new File(folder, "kosbuild.json");
        RunPluginCommandLine runPluginData = new RunPluginCommandLine();
        runPluginData.setPluginNameVersion("gccProjectInfo:0.1");
        Config.init(new File("./kosbuild/conf/configuration.json"));
        PluginResults pluginResults = new KOSBuild().runPlugin(buildFile, runPluginData);
        Object pluginResultObject = pluginResults.getResults().get(0).getValue();
        ProjectStructure projectInfo = Utils.reconvertWithJson(pluginResultObject, ProjectStructure.class);
        projectInfo.setRootFile(initProjectFileSystem(projectInfo));

        return projectInfo;
    }

    private AbstractVNode initProjectFileSystem(ProjectStructure projectStructure) {
        VirtualFileVNode rootNode = new VirtualFileVNode();
        rootNode.setName(projectStructure.getProjectName());
        rootNode.setIcon(ImageManager.get().getIcon("eagle"));
        return rootNode;
    }
}
