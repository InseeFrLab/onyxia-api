package fr.insee.onyxia.model.service.quota;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;

@Schema(
        description =
                "Resource quotas are a tool for administrators to address the fair sharing of cluster resources between namespaces https://kubernetes.io/docs/concepts/policy/resource-quotas/")
public class Quota {

    private static final String REQUESTS_MEMORY = "requests.memory";
    private static final String REQUESTS_CPU = "requests.cpu";
    private static final String LIMITS_MEMORY = "limits.memory";
    private static final String LIMITS_CPU = "limits.cpu";
    private static final String REQUESTS_STORAGE = "requests.storage";
    private static final String COUNT_PODS = "count/pods";
    private static final String REQUESTS_EPHEMERAL_STORAGE = "requests.ephemeral-storage";
    private static final String LIMITS_EPHEMERAL_STORAGE = "limits.ephemeral-storage";
    private static final String REQUESTS_NVIDIA_COM_GPU = "requests.nvidia.com/gpu";
    private static final String LIMITS_NVIDIA_COM_GPU = "limits.nvidia.com/gpu";

    @JsonProperty(REQUESTS_MEMORY)
    @Schema(
            description =
                    "Across all pods in a non-terminal state, the sum of memory requests cannot exceed this value.")
    private String memoryRequests;

    @Schema(
            description =
                    "Across all pods in a non-terminal state, the sum of CPU requests cannot exceed this value.")
    @JsonProperty(REQUESTS_CPU)
    private String cpuRequests;

    @Schema(
            description =
                    "Across all pods in a non-terminal state, the sum of memory limits cannot exceed this value.")
    @JsonProperty(LIMITS_MEMORY)
    private String memoryLimits;

    @Schema(
            description =
                    "Across all pods in a non-terminal state, the sum of CPU limits cannot exceed this value.")
    @JsonProperty(LIMITS_CPU)
    private String cpuLimits;

    @Schema(
            description =
                    "Across all persistent volume claims, the sum of storage requests cannot exceed this value.")
    @JsonProperty(REQUESTS_STORAGE)
    private String storageRequests;

    @Schema(description = "The number of pod in the namespace cannot exceed this value.")
    @JsonProperty(COUNT_PODS)
    private Integer podsCount;

    @Schema(description = "The request ephemeralStorage allowed")
    @JsonProperty(REQUESTS_EPHEMERAL_STORAGE)
    private String ephemeralStorageRequests;

    @Schema(description = "The limit ephemeralStorage allowed")
    @JsonProperty(LIMITS_EPHEMERAL_STORAGE)
    private String ephemeralStorageLimits;

    @Schema(description = "The request nvidia gpu")
    @JsonProperty(REQUESTS_NVIDIA_COM_GPU)
    private Integer nvidiaGpuRequests;

    @Schema(description = "The limit nvidia gpu")
    @JsonProperty(LIMITS_NVIDIA_COM_GPU)
    private Integer nvidiaGpuLimits;

    public Map<String, String> asMap() {
        final Map<String, String> quotas = new HashMap<>();
        quotas.put(REQUESTS_MEMORY, getMemoryRequests());
        quotas.put(REQUESTS_CPU, getCpuRequests());
        quotas.put(LIMITS_MEMORY, getMemoryLimits());
        quotas.put(LIMITS_CPU, getCpuLimits());
        quotas.put(REQUESTS_STORAGE, getStorageRequests());
        quotas.put(COUNT_PODS, getPodsCount() == null ? null : String.valueOf(getPodsCount()));
        quotas.put(REQUESTS_EPHEMERAL_STORAGE, getEphemeralStorageRequests());
        quotas.put(LIMITS_EPHEMERAL_STORAGE, getEphemeralStorageLimits());
        quotas.put(
                REQUESTS_NVIDIA_COM_GPU,
                getNvidiaGpuRequests() == null ? null : String.valueOf(getNvidiaGpuRequests()));
        quotas.put(
                LIMITS_NVIDIA_COM_GPU,
                getNvidiaGpuLimits() == null ? null : String.valueOf(getNvidiaGpuLimits()));
        return quotas;
    }

    public static Quota from(Map<String, String> data) {
        Quota quota = new Quota();
        quota.setMemoryRequests(data.get(REQUESTS_MEMORY));
        quota.setCpuRequests(data.get(REQUESTS_CPU));
        quota.setMemoryLimits(data.get(LIMITS_MEMORY));
        quota.setCpuLimits(data.get(LIMITS_CPU));
        quota.setEphemeralStorageRequests(data.get(REQUESTS_EPHEMERAL_STORAGE));
        quota.setEphemeralStorageLimits(data.get(LIMITS_EPHEMERAL_STORAGE));
        quota.setStorageRequests(data.get(REQUESTS_STORAGE));
        if (data.containsKey(REQUESTS_NVIDIA_COM_GPU)) {
            quota.setNvidiaGpuRequests(Integer.parseInt(data.get(REQUESTS_NVIDIA_COM_GPU)));
        }
        if (data.containsKey(LIMITS_NVIDIA_COM_GPU)) {
            quota.setNvidiaGpuLimits(Integer.parseInt(data.get(LIMITS_NVIDIA_COM_GPU)));
        }
        quota.setPodsCount(
                data.get(COUNT_PODS) == null ? null : Integer.valueOf(data.get(COUNT_PODS)));
        return quota;
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
