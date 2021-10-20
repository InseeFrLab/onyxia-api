package fr.insee.onyxia.model.service.quota;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Quota {

    @JsonProperty("requests.memory")
    private String memoryRequests;

    @JsonProperty("requests.cpu")
    private String cpuRequests;

    @JsonProperty("limits.memory")
    private String memoryLimits;

    @JsonProperty("limits.cpu")
    private String cpuLimits;

    @JsonProperty("requests.storage")
    private String storageRequests;

    @JsonProperty("count/pods")
    private Integer podsCount;

    public Map<String, String> asMap() {
        Map<String, String> quotas = new HashMap<>();
        quotas.put("requests.memory",getMemoryRequests());
        quotas.put("requests.cpu", getCpuRequests());
        quotas.put("limits.memory", getMemoryLimits());
        quotas.put("limits.cpu", getCpuLimits());
        quotas.put("requests.storage", getStorageRequests());
        quotas.put("count/pods", getPodsCount() == null ? null : String.valueOf(getPodsCount()));
        return quotas;
    }

    public void loadFromMap(Map<String, String> data) {
        setMemoryRequests(data.get("requests.memory"));
        setCpuRequests(data.get("requests.cpu"));
        setMemoryLimits(data.get("limits.memory"));
        setCpuLimits(data.get("limits.cpu"));
        setStorageRequests(data.get("requests.storage"));
        setPodsCount(data.get("count/pods") == null ? null : Integer.valueOf(data.get("count/pods")));
    }

    public String getMemoryRequests() {
        return memoryRequests;
    }

    public void setMemoryRequests(String memoryRequests) {
        this.memoryRequests = memoryRequests;
    }

    public String getCpuRequests() {
        return cpuRequests;
    }

    public void setCpuRequests(String cpuRequests) {
        this.cpuRequests = cpuRequests;
    }

    public String getMemoryLimits() {
        return memoryLimits;
    }

    public void setMemoryLimits(String memoryLimits) {
        this.memoryLimits = memoryLimits;
    }

    public String getCpuLimits() {
        return cpuLimits;
    }

    public void setCpuLimits(String cpuLimits) {
        this.cpuLimits = cpuLimits;
    }

    public String getStorageRequests() {
        return storageRequests;
    }

    public void setStorageRequests(String storageRequests) {
        this.storageRequests = storageRequests;
    }

    public Integer getPodsCount() {
        return podsCount;
    }

    public void setPodsCount(Integer podsCount) {
        this.podsCount = podsCount;
    }
}