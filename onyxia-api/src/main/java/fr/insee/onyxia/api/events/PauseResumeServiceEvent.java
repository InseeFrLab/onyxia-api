package fr.insee.onyxia.api.events;

public class PauseResumeServiceEvent extends OnyxiaEvent {

    private String type;
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

    public PauseResumeServiceEvent() {}

    public PauseResumeServiceEvent(
            String username,
            String namespace,
            String releaseName,
            String packageName,
            String catalogId,
            boolean pause) {
        this.namespace = namespace;
        this.releaseName = releaseName;
        this.catalogId = catalogId;
        this.username = username;
        this.packageName = packageName;
        if (pause) {
            type = "service.pause";
        } else {
            type = "service.resume";
        }
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
        return type;
    }
}
