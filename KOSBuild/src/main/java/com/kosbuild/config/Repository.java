package com.kosbuild.config;

/**
 * @author Dmitry
 */
public class Repository {

    private String path;
    private String name;

    public Repository(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

}
