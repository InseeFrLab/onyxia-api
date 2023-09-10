package fr.insee.onyxia.model.service.quota;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
	description =
	"In the namespace en suivant les notations kubernetes https://kubernetes.io/docs/concepts/policy/resource-quotas/ . Si laissé vide pas de changement stays untouched")
public class Quota {

    @JsonProperty("requests.memory")
    @Schema(
	    description =
	    "Across all pods in a non-terminal state, the sum of memory requests cannot exceed this value.")
    private String memoryRequests;

    @Schema(
	    description =
	    "Across all pods in a non-terminal state, the sum of CPU requests cannot exceed this value.")
    @JsonProperty("requests.cpu")
    private String cpuRequests;

    @Schema(
	    description =
	    "Across all pods in a non-terminal state, the sum of memory limits cannot exceed this value.")
    @JsonProperty("limits.memory")
    private String memoryLimits;

    @Schema(
	    description =
	    "Across all pods in a non-terminal state, the sum of CPU limits cannot exceed this value.")
    @JsonProperty("limits.cpu")
    private String cpuLimits;

    @Schema(
	    description =
	    "Across all persistent volume claims, the sum of storage requests cannot exceed this value.")
    @JsonProperty("requests.storage")
    private String storageRequests;

    @Schema(description = "The number of pod in the namespace cannot exceed this value.")
    @JsonProperty("count/pods")
    private Integer podsCount;

    @Schema(description = "The request ephemeralStorage allowed")
    @JsonProperty("requests.ephemeral-storage")
    private String ephemeralStorageRequests;

    @Schema(description = "The request ephemeralStorage allowed")
    @JsonProperty("limits.ephemeral-storage")
    private String ephemeralStorageLimits;

    @Schema(description = "The request nvidia gpu")
    @JsonProperty("requests.nvidia.com/gpu")
    private Integer nvidiaGpuRequests;

    @Schema(description = "The limit nvidia gpu")
    @JsonProperty("limits.nvidia.com/gpu")
    private Integer nvidiaGpuLimits;

    public Map<String, String> asMap() {
	final Map<String, String> quotas = new HashMap<>();
	quotas.put("requests.memory", getMemoryRequests());
	quotas.put("requests.cpu", getCpuRequests());
	quotas.put("limits.memory", getMemoryLimits());
	quotas.put("limits.cpu", getCpuLimits());
	quotas.put("requests.storage", getStorageRequests());
	quotas.put("count/pods", getPodsCount() == null ? null : String.valueOf(getPodsCount()));
	quotas.put("requests.ephemeral-storage",getEphemeralStorageLimits());
	quotas.put("limits.ephemeral-storage",getEphemeralStorageLimits());
	quotas.put("requests.nvidia.com/gpu",getNvidiaGpuRequests() == null ? null : String.valueOf(getNvidiaGpuRequests()));
	quotas.put("limits.nvidia.com/gpu",getNvidiaGpuLimits() == null ? null : String.valueOf(getNvidiaGpuLimits()));
	return quotas;
    }

    public void loadFromMap(Map<String, String> data) {
	setMemoryRequests(data.get("requests.memory"));
	setCpuRequests(data.get("requests.cpu"));
	setMemoryLimits(data.get("limits.memory"));
	setCpuLimits(data.get("limits.cpu"));
	setStorageRequests(data.get("requests.storage"));
	setPodsCount(
		data.get("count/pods") == null ? null : Integer.valueOf(data.get("count/pods")));
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

    public String getEphemeralStorageRequests() {
	return ephemeralStorageRequests;
    }

    public void setEphemeralStorageRequests(String ephemeralStorageRequests) {
	this.ephemeralStorageRequests = ephemeralStorageRequests;
    }

    public String getEphemeralStorageLimits() {
	return ephemeralStorageLimits;
    }

    public void setEphemeralStorageLimits(String ephemeralStorageLimits) {
	this.ephemeralStorageLimits = ephemeralStorageLimits;
    }

    public Integer getNvidiaGpuRequests() {
	return nvidiaGpuRequests;
    }

    public void setNvidiaGpuRequests(Integer nvidiaGpuRequests) {
	this.nvidiaGpuRequests = nvidiaGpuRequests;
    }

    public Integer getNvidiaGpuLimits() {
	return nvidiaGpuLimits;
    }

    public void setNvidiaGpuLimits(Integer nvidiaGpuLimits) {
	this.nvidiaGpuLimits = nvidiaGpuLimits;
    }


}
