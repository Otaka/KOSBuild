package com.kosbuild.config;

import com.kosbuild.dependencies.Dependency;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class CrossModuleProperties {

    private List<Dependency> listOfDependenciesWithDisabledTransient = new ArrayList<>();

    public List<Dependency> getListOfDependenciesWithDisabledTransient() {
        return listOfDependenciesWithDisabledTransient;
    }

}
