package fr.insee.onyxia.model.service;


import java.util.ArrayList;
import java.util.List;

public class Task {

    private String id;
    private TaskStatus status;
    private List<Container> containers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }
}
