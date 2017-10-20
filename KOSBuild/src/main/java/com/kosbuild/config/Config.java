package com.kosbuild.config;

import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;
import com.kosbuild.jsonparser.JsonParser;
import com.kosbuild.utils.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * @author Dmitry
 */
public class Config {

    static final Logger log = Utils.getLogger();
    private static Config instance;

    private Repository localRepository;
    private List<Repository> remoteRepositories = new ArrayList<>();

    public static Config get() {
        if (instance == null) {
            throw new IllegalStateException("You have not run Config.init(configuration.json) in KOSBuild");
        }

        return instance;
    }

    public Repository getLocalRepository() {
        return localRepository;
    }

    public List<Repository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public static void init(File configFile) throws FileNotFoundException {
        if (instance != null) {
            return;
        }
        if (!configFile.exists()) {
            throw new IllegalStateException("Cannot find configuration file [" + configFile + "]");
        }

        JsonParser parser = new JsonParser();
        JsonObject element = parser.parse(new FileReader(configFile)).getAsObject();
        if (!element.contains("repositories")) {
            throw new IllegalArgumentException("Config file [" + configFile.getAbsolutePath() + "] does not contain section [repositories]");
        }

        instance = new Config();
        instance.parseRepositories(element.getElementByName("repositories").getAsObject(), configFile);
    }

    private void parseRepositories(JsonObject repositoryConfig, File configFile) {
        if (!repositoryConfig.contains("local")) {
            throw new IllegalArgumentException("Section [repositories] in config file [" + configFile.getAbsolutePath() + "] does not contain subsection [local]");
        }

        localRepository = parseRepository(repositoryConfig.getElementByName("local").getAsObject(), true, "localRepository_", 0);
        if (!repositoryConfig.contains("remote")) {
            log.warn("There is no section [repositories.remote] in config file [" + configFile.getAbsolutePath() + "]. \nEnter in offline mode!");
        } else {
            JsonArray remoteRepositoriesArrayJson = repositoryConfig.getElementByName("remote").getAsArray();
            int repositoryIndex = 1;
            for (JsonElement remoteRepositoryElement : remoteRepositoriesArrayJson.getElements()) {
                JsonObject rep = remoteRepositoryElement.getAsObject();
                remoteRepositories.add(parseRepository(rep, false, "remoteRepository_", repositoryIndex));
                if (!rep.contains("name")) {
                    repositoryIndex++;
                }
            }
        }
    }

    private Repository parseRepository(JsonObject repositoryObject, boolean checkLocalPath, String baseName, int repositoryIndex) {
        String repositoryPath = repositoryObject.getElementByName("path").getAsString();
        String repositoryName;
        if (!repositoryObject.contains("name")) {
            repositoryName = baseName + repositoryIndex;
        } else {
            repositoryName = repositoryObject.getElementByName("name").getAsString();
        }

        if (checkLocalPath) {
            if (!new File(repositoryPath).exists()) {
                throw new IllegalArgumentException("Path to the local repository [repository.local] in the config file [" + repositoryPath + "] does not exist");
            }
        }
        return new Repository(repositoryPath, repositoryName);
    }
}
