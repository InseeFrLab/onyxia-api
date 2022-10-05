package fr.insee.onyxia.model.service;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")
public class Group {

    @Schema(description = "")
    private String id;
    @Schema(description = "")
    private List<Service> apps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Service> getApps() {
        return apps;
    }

    public void setApps(List<Service> apps) {
        this.apps = apps;
    }
}
