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
