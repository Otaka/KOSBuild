package org.visualeagle.project.projectloaders;

import java.io.File;

/**
 * @author Dmitry
 */
public abstract class ProjectLoader {

    public abstract ProjectStructure loadProject(File folder) throws Exception;
}
