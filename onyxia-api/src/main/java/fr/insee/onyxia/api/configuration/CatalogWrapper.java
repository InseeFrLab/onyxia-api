package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fr.insee.onyxia.model.catalog.CatalogStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogWrapper {

    private fr.insee.onyxia.model.catalog.CatalogWrapper catalog;
    private String id;
    private String name;
    private String description;
    private String maintainer;
    private String location;
    private CatalogStatus status;
    private long lastUpdateTime;
    private String scm;
    private String type;

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the catalog
     */
    public fr.insee.onyxia.model.catalog.CatalogWrapper getCatalog() {
        return catalog;
    }

    /**
     * @param catalog the catalog to set
     */
    public void setCatalog(fr.insee.onyxia.model.catalog.CatalogWrapper catalog) {
        this.catalog = catalog;
    }

    public CatalogStatus getStatus() {
        return status;
    }

    public void setStatus(CatalogStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
