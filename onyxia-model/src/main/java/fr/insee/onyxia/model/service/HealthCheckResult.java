package fr.insee.onyxia.model.service;

import java.util.Map;

public class HealthCheckResult {
    private boolean healthy;
    private String name;
    private String kind;
    private Map<String, Object> details;

    public HealthCheckResult(
            boolean healthy, String name, String kind, Map<String, Object> details) {
        this.healthy = healthy;
        this.name = name;
        this.kind = kind;
        this.details = details;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
