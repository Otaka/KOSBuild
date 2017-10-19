package com.kosbuild.gccprojectinfoplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class ProjectInfo {

    private List<String> includePaths = new ArrayList<>();
    private List<String> libraryPaths = new ArrayList<>();
    private List<String> librariesNames = new ArrayList<>();
    private List<String> sourcePaths=new ArrayList<>();
    private File buildFile;

    public void setBuildFile(File buildFile) {
        this.buildFile = buildFile;
    }

    public File getBuildFile() {
        return buildFile;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }
    

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public List<String> getLibrariesNames() {
        return librariesNames;
    }

    public List<String> getLibraryPaths() {
        return libraryPaths;
    }
    
    
}
