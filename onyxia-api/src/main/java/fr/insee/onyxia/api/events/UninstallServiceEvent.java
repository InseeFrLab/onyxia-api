package fr.insee.onyxia.api.events;

public class UninstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.uninstall";

    private String namespace;

    private String releaseName;

    private String username;

    public UninstallServiceEvent() {}

    public UninstallServiceEvent(String namespace, String releaseName, String username) {
        this.namespace = namespace;
        this.releaseName = releaseName;
        this.username = username;
    }

    @Override
    public String getType() {
        return TYPE;
    }


    public String getNamespace() {
        return namespace;
    }

    public String getUsername() {
        return username;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

}
