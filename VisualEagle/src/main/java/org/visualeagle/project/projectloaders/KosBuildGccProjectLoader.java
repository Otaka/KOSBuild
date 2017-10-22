package org.visualeagle.project.projectloaders;

import com.kosbuild.KOSBuild;
import com.kosbuild.config.Config;
import com.kosbuild.config.RunPluginCommandLine;
import com.kosbuild.plugins.PluginResults;
import com.kosbuild.utils.Utils;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.project.vnodes.LocalFolderVNode;
import org.visualeagle.project.vnodes.VirtualFolderVNode;
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
        projectInfo.setRootProjectVFile(initProjectFileSystem(projectInfo));

        return projectInfo;
    }

    private AbstractVNode initProjectFileSystem(ProjectStructure projectStructure) {
        VirtualFolderVNode rootNode = new VirtualFolderVNode(projectStructure.getProjectName());
        rootNode.setIcon(ImageManager.get().getIcon("eagle_small"));
        LocalFolderVNode sourceFolder = new LocalFolderVNode("Sources", new File(projectStructure.getSourcePaths().get(0)));
        
        LocalFolderVNode includeFolder = new LocalFolderVNode("Headers", new File(projectStructure.getIncludePath()));

        VirtualFolderVNode dependencies = new VirtualFolderVNode("Dependencies");
        VirtualFolderVNode dependenciesHeaders = new VirtualFolderVNode("Headers");
        dependencies.addChild(dependenciesHeaders);
        for (String dependenciesHeadersFolderPath : projectStructure.getDependenciesIncludePaths()) {
            String folderName = StringUtils.abbreviateMiddle(dependenciesHeadersFolderPath, "...", 100);
            LocalFolderVNode dependenciesHeadersFolder = new LocalFolderVNode(folderName, new File(dependenciesHeadersFolderPath));
            dependenciesHeadersFolder.setReadonly(true);
            dependenciesHeaders.addChild(dependenciesHeadersFolder);
        }

        VirtualFolderVNode dependencyLibrariesPath = new VirtualFolderVNode("Libraries Paths");
        dependencies.addChild(dependencyLibrariesPath);
        for (String dependencLibrariesFolderPath : projectStructure.getDependenciesLibraryPaths()) {
            String folderName = StringUtils.abbreviateMiddle(dependencLibrariesFolderPath, "...", 100);
            LocalFolderVNode dependencyLibrariesFolder = new LocalFolderVNode(folderName, new File(dependencLibrariesFolderPath));
            dependencyLibrariesPath.addChild(dependencyLibrariesFolder);
        }

        rootNode.addChild(sourceFolder);
        rootNode.addChild(includeFolder);
        rootNode.addChild(dependencies);
        rootNode.setUserObject(projectStructure);
        return rootNode;
    }
}
