package fr.insee.onyxia.api.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.catalog.CatalogStatus;
import fr.insee.onyxia.model.helm.Repository;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "A catalog with its metadatas")
public class CatalogWrapper {

    @Schema(description = "Catalog")
    private fr.insee.onyxia.model.catalog.CatalogWrapper catalog = new Repository();

    @Schema(description = "Catalog id")
    private String id;

    @Schema(description = "Catalog name")
    private String name;

    @Schema(description = "Description of the catalog")
    private String description;

    @Schema(description = "Who maintains the catalog")
    private String maintainer;

    @Schema(description = "Where to find the catalog")
    private String location;

    @Schema(description = "Is the catalog a test, an internal or a production catalog")
    private CatalogStatus status;

    @Schema(description = "Last date the catalog was updated")
    private long lastUpdateTime;

    @Schema(description = "?")
    private String scm;

    @Schema(description = "helm or catalog will not be considered as enabled")
    private String type;

    @Schema(description = "name of the charts that will not be fetch or reload")
    private List<String> excludedCharts = new ArrayList<>();

    @Schema(description = "names of declarative important charts of the catalog")
    private List<String> highlightedCharts = new ArrayList<>();

    @Schema(description = "Skip tls certificate checks for the repository")
    private boolean skipTlsVerify;

    @Schema(description = "Verify certificates of HTTPS-enabled servers using this CA bundle")
    private String caFile;

    @Schema(description = "Allow sharing this service within a project")
    private boolean allowSharing = true;

    @Schema(description = "Should this catalog be visible in user context ? Project context ?")
    private CatalogVisibility visible = new CatalogVisibility();

    @Schema(description = "If a chart has multiple versions, which one(s) to keep")
    private MultipleServicesMode multipleServicesMode = MultipleServicesMode.LATEST;

    @Schema(description = "If multipleServicesMode is set to maxNumber, how many versions to keep")
    private int maxNumberOfVersions = 5;

    public enum MultipleServicesMode {
        @JsonProperty("all")
        ALL,
        @JsonProperty("latest")
        LATEST,
        @JsonProperty("skipPatches")
        SKIP_PATCHES,
        @JsonProperty("maxNumber")
        MAX_NUMBER
    }

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

    public List<String> getExcludedCharts() {
        return excludedCharts;
    }

    public void setExcludedCharts(List<String> excludedCharts) {
        this.excludedCharts = excludedCharts;
    }

    public List<String> getHighlightedCharts() {
        return highlightedCharts;
    }

    public void setHighlightedCharts(List<String> highlightedCharts) {
        this.highlightedCharts = highlightedCharts;
    }

    public boolean getSkipTlsVerify() {
        return skipTlsVerify;
    }

    public void setSkipTlsVerify(boolean skipTlsVerify) {
        this.skipTlsVerify = skipTlsVerify;
    }

    public String getCaFile() {
        return caFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }

    public boolean isAllowSharing() {
        return allowSharing;
    }

    public void setAllowSharing(boolean allowSharing) {
        this.allowSharing = allowSharing;
    }

    public CatalogVisibility getVisible() {
        return visible;
    }

    public void setVisible(CatalogVisibility visible) {
        this.visible = visible;
    }

    public MultipleServicesMode getMultipleServicesMode() {
        return multipleServicesMode;
    }

    public void setMultipleServicesMode(MultipleServicesMode multipleServicesMode) {
        this.multipleServicesMode = multipleServicesMode;
    }

    public int getMaxNumberOfVersions() {
        return maxNumberOfVersions;
    }

    public void setMaxNumberOfVersions(int maxNumberOfVersions) {
        this.maxNumberOfVersions = maxNumberOfVersions;
    }

    public static class CatalogVisibility {

        private boolean user = true;
        private boolean project = true;

        public boolean isUser() {
            return user;
        }

        public void setUser(boolean user) {
            this.user = user;
        }

        public boolean isProject() {
            return project;
        }

        public void setProject(boolean project) {
            this.project = project;
        }
    }
}
