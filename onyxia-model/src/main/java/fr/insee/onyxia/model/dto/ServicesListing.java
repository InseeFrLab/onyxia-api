package fr.insee.onyxia.model.dto;

import fr.insee.onyxia.model.service.Group;
import fr.insee.onyxia.model.service.Service;

import java.util.ArrayList;
import java.util.List;

public class ServicesListing {

    private List<Service> apps = new ArrayList<>();

    private List<Group> groups = new ArrayList<>();

    public List<Service> getApps() {
        return apps;
    }

    public void setApps(List<Service> apps) {
        this.apps = apps;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
