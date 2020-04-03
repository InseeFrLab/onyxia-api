package fr.insee.onyxia.model.catalog;

public class Package {
    private String name;
    private String description;
    private String version;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Package name(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Package description(String description) {
        this.description = description;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Package version(String version) {
        this.version = version;
        return this;
    }

}