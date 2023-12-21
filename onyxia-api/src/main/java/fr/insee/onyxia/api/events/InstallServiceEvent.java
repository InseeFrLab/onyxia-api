package fr.insee.onyxia.api.events;

import org.springframework.stereotype.Component;

@Component
public class InstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.install";
    private String namespace;
    private String name;
    private String username;
    private String catalogId;

    public InstallServiceEvent() {
    }

    public InstallServiceEvent(String username, String namespace, String name, String catalogId) {
        this.namespace = namespace;
        this.name = name;
        this.catalogId = catalogId;
        this.username = username;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername(){ return username; }

    public void setUsername(String username){this.username = username;}
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
