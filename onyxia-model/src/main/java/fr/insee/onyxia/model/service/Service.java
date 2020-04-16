package fr.insee.onyxia.model.service;

import java.util.List;
import java.util.Map;

public class Service {

    private String id, name;
    private int instances;
    private double cpus;
    private double mem;
    private ServiceStatus status = ServiceStatus.RUNNING;
    private ServiceType type;
    private List<String> urls;

    private long startedAt;

    private Map<String,String> labels;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public double getMem() {
        return mem;
    }

    public void setMem(double mem) {
        this.mem = mem;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public static enum ServiceStatus {
        DEPLOYING, RUNNING, STOPPED;
    }

    public static enum ServiceType {
        KUBERNETES,MARATHON
    }
}
