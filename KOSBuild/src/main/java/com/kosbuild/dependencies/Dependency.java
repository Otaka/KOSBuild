package com.kosbuild.dependencies;

import java.util.Objects;

/**
 * @author Dmitry
 */
public class Dependency {

    private String name;
    private String version;
    private String compiler;
    private boolean transitive;
    private boolean includeWithoutPrefix;

    public String formatPath() {
        if (compiler == null) {
            return name + "/" + version + "/";
        } else {
            return name + "/" + version + "/" + compiler + "/";
        }
    }

    public void setIncludeWithoutPrefix(boolean includeWithoutPrefix) {
        this.includeWithoutPrefix = includeWithoutPrefix;
    }

    public boolean isIncludeWithoutPrefix() {
        return includeWithoutPrefix;
    }
    
    

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    public boolean isTransitive() {
        return transitive;
    }
    
    

    public String getName() {
        return name;
    }

    public Dependency setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Dependency setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCompiler() {
        return compiler;
    }

    public Dependency setCompiler(String compiler) {
        this.compiler = compiler;
        return this;
    }

    @Override
    public String toString() {
        if(compiler==null){
            return name + ":" + version;
        }
        return name + ":" + version + ":" + compiler;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.version);
        hash = 43 * hash + Objects.hashCode(this.compiler);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dependency other = (Dependency) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.compiler, other.compiler)) {
            return false;
        }
        return true;
    }

}
