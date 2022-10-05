package fr.insee.onyxia.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")
public class Service {

    @Schema(description = "")
    private String id;
    @Schema(description = "")
    private String name;
    @Schema(description = "")
    private int instances;
    @Schema(description = "")
    private double cpus;
    @Schema(description = "")
    private double mem;
    @Schema(description = "")
    private String status;
    @Schema(description = "")
    private ServiceType type;
    @Schema(description = "")
    private List<String> urls;
    @Schema(description = "")
    private List<String> internalUrls;
    @Schema(description = "")
    private Map<String, String> env = new HashMap<>();
    @Schema(description = "")
    private List<Task> tasks = new ArrayList<>();
    @Schema(description = "")
    private List<Event> events = new ArrayList<>();
    @Schema(description = "")
    private String subtitle;
    @Schema(description = "")
    private Monitoring monitoring;
    @Schema(description = "")
    private String postInstallInstructions;
    @Schema(description = "")
    private String namespace;
    @Schema(description = "")
    private String revision;
    @Schema(description = "")
    private String updated;
    @Schema(description = "")
    private String appVersion;

    @Schema(description = "")
    private long startedAt;

    @Schema(description = "")
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<String> getInternalUrls() {
        return internalUrls;
    }

    public void setInternalUrls(List<String> internalUrls) {
        this.internalUrls = internalUrls;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setPostInstallInstructions(String postInstallInstructions) {
        this.postInstallInstructions = postInstallInstructions;
    }

    public String getPostInstallInstructions() {
        return postInstallInstructions;
    }

	@Schema(description = "Cloudshell data and health")
    public static enum ServiceStatus {
        DEPLOYING, RUNNING, STOPPED;
    }

	@Schema(description = "Cloudshell data and health")
    public static enum ServiceType {
        KUBERNETES
    }

	@Schema(description = "Cloudshell data and health")
    public static class Monitoring {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
