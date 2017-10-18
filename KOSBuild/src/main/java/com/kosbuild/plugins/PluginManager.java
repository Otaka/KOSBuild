package com.kosbuild.plugins;

import com.kosbuild.dependencies.Dependency;
import com.kosbuild.dependencies.DependencyExtractor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Dmitry
 */
public class PluginManager {

    private static PluginManager instance = new PluginManager();
    private Map<File, Class> pathToPluginClassMap = new HashMap<>();
    private Map<String, PluginConfig> pluginNameToPluginConfigMap = new HashMap<>();

    private PluginManager() {
    }

    public static PluginManager get() {
        return instance;
    }

    public PluginConfig loadPluginConfig(String pluginNameVersion) throws IOException {
        if (pluginNameToPluginConfigMap.containsKey(pluginNameVersion)) {
            return pluginNameToPluginConfigMap.get(pluginNameVersion);
        }

        if (!pluginNameVersion.contains(":")) {
            throw new IllegalArgumentException("Plugin name [" + pluginNameVersion + "] is in wrong format. Proper format \"NAME:VERSION\"");
        }

        String name = pluginNameVersion.substring(0, pluginNameVersion.indexOf(':'));
        String version = pluginNameVersion.substring(pluginNameVersion.indexOf(':') + 1);
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setName(name);
        pluginConfig.setVersion(version);
        File folderWithPlugin = new DependencyExtractor().getPathToPluginDependencyAndLoadIfNotExists(new Dependency().setName(name).setVersion(version));
        pluginConfig.setPluginLocalRepositoryFolder(folderWithPlugin);
        AbstractPlugin pluginObject = loadPlugin(folderWithPlugin);
        pluginConfig.setDynamicPluginObject(pluginObject);
        pluginConfig.setOverrideRunOnSteps(new String[]{"RUN_FROM_CODE"});
        pluginNameToPluginConfigMap.put(pluginNameVersion, pluginConfig);
        pluginObject.init();
        return pluginConfig;
    }

    public AbstractPlugin loadPlugin(File localRepositoryFolder) throws IOException {
        if (pathToPluginClassMap.containsKey(localRepositoryFolder)) {
            try {
                Class pluginClass = pathToPluginClassMap.get(localRepositoryFolder);
                return (AbstractPlugin) pluginClass.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Error while instantiate plugin [" + localRepositoryFolder.getAbsolutePath() + "]", ex);
            }
        }

        File jarFile = searchPluginJarFile(localRepositoryFolder);
        String pluginMainClassPath = searchMainPluginClassInJar(jarFile);
        if (pluginMainClassPath == null) {
            throw new IllegalStateException("Plugin [" + localRepositoryFolder.getAbsolutePath() + "] does not have class PluginMain in jar file [" + jarFile.getAbsolutePath() + "]");
        }
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{jarFile.toURI().toURL()},
                    getClass().getClassLoader()
            );

            Class<?> clazz = Class.forName(pluginMainClassPath, true, loader);
            Object pluginInstance = clazz.newInstance();
            if (!(pluginInstance instanceof AbstractPlugin)) {
                throw new IllegalStateException("Plugin [" + jarFile.getAbsolutePath() + "] has main class [" + pluginMainClassPath + "] but this class not extends [" + AbstractPlugin.class.getName() + "]");
            }

            pathToPluginClassMap.put(localRepositoryFolder, clazz);
            return (AbstractPlugin) pluginInstance;
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Cannot load class [" + pluginMainClassPath + "] from the plugin jar file [" + jarFile.getAbsolutePath() + "]", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot instantiate class [" + pluginMainClassPath + "] from the plugin jar file [" + jarFile.getAbsolutePath() + "]", ex);
        }
    }

    /*
    class should have name PluginMain
     */
    private String searchMainPluginClassInJar(File jarFile) throws IOException {
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> enumJarEntries = jar.entries();
        while (enumJarEntries.hasMoreElements()) {
            JarEntry jarEntry = enumJarEntries.nextElement();
            if (jarEntry.getName().endsWith("PluginMain.class")) {
                String path = jarEntry.getName();
                return path.replace('/', '.').substring(0, path.length() - ".class".length());
            }
        }
        return null;
    }

    private File searchPluginJarFile(File localRepositoryFolder) {
        File jarFolder = new File(localRepositoryFolder, "jar");
        if (!jarFolder.exists()) {
            throw new IllegalStateException("Plugin [" + localRepositoryFolder + "] does not have folder [jar] with jar file");
        }

        List<File> jarFiles = new ArrayList<>();
        for (File file : jarFolder.listFiles()) {
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jar")) {
                jarFiles.add(file);
            }
        }

        if (jarFiles.size() > 1) {
            throw new IllegalStateException("Plugin [" + localRepositoryFolder + "] can contain only one jar file in jar folder, but found [" + jarFiles.size() + "] files. " + jarFiles.toString());
        }

        if (jarFiles.isEmpty()) {
            throw new IllegalStateException("Plugin [" + localRepositoryFolder + "] can do not have any jar files in jar folder");
        }

        return jarFiles.get(0);
    }
}
