package org.visualeagle.project;

import org.visualeagle.project.projectloaders.ProjectStructure;

/**
 * @author sad
 */
public class ProjectManager {

    private ProjectStructure projectStructure;

    public ProjectStructure getCurrentProject() {
        return projectStructure;
    }

    public void closeCurrentProject() {
        projectStructure = null;
    }

}
