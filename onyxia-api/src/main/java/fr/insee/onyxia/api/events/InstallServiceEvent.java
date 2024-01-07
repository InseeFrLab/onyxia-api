package fr.insee.onyxia.api.events;

public class InstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.install";
    private String namespace;
    private String releaseName;
    private String packageName;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    private String username;
    private String catalogId;

    public InstallServiceEvent() {}

    public InstallServiceEvent(
            String username,
            String namespace,
            String releaseName,
            String packageName,
            String catalogId) {
        this.namespace = namespace;
        this.releaseName = releaseName;
        this.catalogId = catalogId;
        this.username = username;
        this.packageName = packageName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
