package org.visualeagle.project;

import org.visualeagle.gui.mainwindow.ActionManager;
import org.visualeagle.project.projectloaders.ProjectStructure;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class ProjectManager {

    private ProjectStructure projectStructure;

    public ProjectManager() {
        ActionManager actionManager=Lookup.get().get(ActionManager.class);
        actionManager.registerAction("clean",this::cleanCurrentProject);
        actionManager.registerAction("build",this::buildCurrentProject);
        actionManager.registerAction("install",this::installCurrentProject);
        actionManager.registerAction("clean_install",this::cleanInstallCurrentProject);
    }

    
    public ProjectStructure getCurrentProject() {
        return projectStructure;
    }

    public void closeCurrentProject() {
        projectStructure = null;
    }

    public void setCurrentProject(ProjectStructure projectStructure) {
        this.projectStructure = projectStructure;
    }
    
    public void cleanCurrentProject(){
    
    }
    public void buildCurrentProject(){
    
    }
    public void installCurrentProject(){
    
    }
    public void cleanInstallCurrentProject(){
    
    }

}
