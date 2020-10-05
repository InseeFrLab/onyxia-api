package fr.insee.onyxia.model.catalog;

import fr.insee.onyxia.model.catalog.Config.Config;

public class Pkg {
    private String name;
    private String description;
    private String version;
    private Config config;

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pkg name(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pkg description(String description) {
        this.description = description;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Pkg version(String version) {
        this.version = version;
        return this;
    }

}