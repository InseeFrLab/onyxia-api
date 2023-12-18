package fr.insee.onyxia.api.events;

import org.springframework.stereotype.Component;

@Component
public class InstallServiceEvent extends OnyxiaEvent {

    public static final String TYPE = "service.install";

    private String namespace;

    private String name;
    private String catalogId;

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }
}
