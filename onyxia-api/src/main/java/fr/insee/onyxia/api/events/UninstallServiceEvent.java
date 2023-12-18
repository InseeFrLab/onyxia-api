package fr.insee.onyxia.api.events;

public class UninstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.uninstall";

    private String namespace;

    private String name;

    @Override
    public String getType() {
        return TYPE;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setName(String name) {
        this.name = name;
    }
}
