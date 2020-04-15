package fr.insee.onyxia.model.dto;

import fr.insee.onyxia.model.service.Service;

import java.util.ArrayList;
import java.util.List;

public class ServicesDTO {

    private List<Service> apps = new ArrayList<>();

    public List<Service> getApps() {
        return apps;
    }

    public void setApps(List<Service> apps) {
        this.apps = apps;
    }
}
