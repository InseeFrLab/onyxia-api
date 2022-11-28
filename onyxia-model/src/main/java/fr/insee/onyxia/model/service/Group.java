package fr.insee.onyxia.model.service;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

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
