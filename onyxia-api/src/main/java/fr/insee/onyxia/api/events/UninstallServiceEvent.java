package fr.insee.onyxia.api.events;

public class UninstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.uninstall";

    private String namespace;

    private String name;

    private String username;
    public UninstallServiceEvent() {
    }

    public UninstallServiceEvent(String namespace, String name, String username) {
        this.namespace = namespace;
        this.name = name;
        this.username = username;
    }

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

    public String getUsername(){
        return username;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username){
        this.username = username;
    }
}
