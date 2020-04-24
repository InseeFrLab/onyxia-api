package fr.insee.onyxia.model.service;

import java.util.List;

public class Group {

    private String id;
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
