package com.kosbuild.gccprojectinfoplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class ProjectInfo {

    private List<File> includePaths = new ArrayList<File>();
    private List<File> libraryPaths = new ArrayList<File>();
    private List<String> librariesNames = new ArrayList<>();

    public List<File> getIncludePaths() {
        return includePaths;
    }

    public List<String> getLibrariesNames() {
        return librariesNames;
    }

    public List<File> getLibraryPaths() {
        return libraryPaths;
    }
    
    
}
