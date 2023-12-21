package fr.insee.onyxia.api.events;

public class InitNamespaceEvent extends OnyxiaEvent {

    public static final String TYPE = "onboarding.createNamespace";
    String region;
    String namespace;
    String username;

    public InitNamespaceEvent() {}

    public InitNamespaceEvent(String region, String namespace, String username) {
        this.region = region;
        this.namespace = namespace;
        this.username = username;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
