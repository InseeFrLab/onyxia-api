package fr.insee.onyxia.api.configuration;

import static fr.insee.onyxia.model.helm.Repository.TYPE_HELM;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.onyxia.model.catalog.CatalogStatus;
import fr.insee.onyxia.model.helm.Repository;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "A catalog with its metadatas")
public class CatalogWrapper {

    @Schema(description = "Catalog")
    private fr.insee.onyxia.model.catalog.CatalogWrapper catalog = new Repository();

    @Schema(description = "Catalog id")
    private String id;

    @Schema(description = "Localized string for the name of the catalog")
    private Object name;

    @Schema(description = "Localized string for the description of the catalog")
    private Object description;

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
    private String type = TYPE_HELM;

    @Schema(description = "name of the charts that will not be fetch or reload")
    private List<String> excludedCharts = new ArrayList<>();

    @Schema(description = "names of declarative important charts of the catalog")
    private List<String> highlightedCharts = new ArrayList<>();

    @Schema(description = "only include charts with one or more of the given keywords")
    private List<String> includeKeywords = new ArrayList<>();

    @Schema(description = "exclude any charts which have one or more of the given keywords")
    private List<String> excludeKeywords = new ArrayList<>();

    @Schema(description = "Skip tls certificate checks for the repository")
    private boolean skipTlsVerify;

    @Schema(description = "value to wait for helm command to complete")
    private String timeout;

    @Schema(description = "Verify certificates of HTTPS-enabled servers using this CA bundle")
    private String caFile;

    @Schema(description = "Allow sharing this service within a project")
    private boolean allowSharing = true;

    @Schema(description = "Should this catalog be visible in user context ? Project context ?")
    private CatalogVisibility visible = new CatalogVisibility();

    @Schema(
            description =
                    "Users must meet the restrictions in order to view and launch services from catalog")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<CatalogRestrictions> restrictions = new ArrayList<>();

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

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
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

    public List<String> getIncludeKeywords() {
        return includeKeywords;
    }

    public void setIncludeKeywords(List<String> includeKeywords) {
        this.includeKeywords = includeKeywords;
    }

    public void setExcludeKeywords(List<String> excludeKeywords) {
        this.excludeKeywords = excludeKeywords;
    }

    public List<String> getExcludeKeywords() {
        return excludeKeywords;
    }

    public boolean getSkipTlsVerify() {
        return skipTlsVerify;
    }

    public void setSkipTlsVerify(boolean skipTlsVerify) {
        this.skipTlsVerify = skipTlsVerify;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
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

    public List<CatalogRestrictions> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<CatalogRestrictions> restrictions) {
        this.restrictions = restrictions;
    }

    public static class CatalogRestrictions {

        private UserAttribute userAttribute;

        public UserAttribute getUserAttribute() {
            return userAttribute;
        }

        public void setUserAttribute(UserAttribute userAttribute) {
            this.userAttribute = userAttribute;
        }

        public static class UserAttribute {
            private String key;
            private Pattern matches;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public Pattern getMatches() {
                return matches;
            }

            public void setMatches(String matches) {
                this.matches = Pattern.compile(matches);
            }
        }
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
