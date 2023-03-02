package io.github.inseefrlab.helmwrapper.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "namespace", "revision", "updated", "status", "chart", "app_version"})
public class HelmLs {

    @JsonProperty("name")
    private String name;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("revision")
    private String revision;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("status")
    private String status;

    @JsonProperty("chart")
    private String chart;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @JsonProperty("revision")
    public String getRevision() {
        return revision;
    }

    @JsonProperty("revision")
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @JsonProperty("updated")
    public String getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("chart")
    public String getChart() {
        return chart;
    }

    @JsonProperty("chart")
    public void setChart(String chart) {
        this.chart = chart;
    }

    @JsonProperty("app_version")
    public String getAppVersion() {
        return appVersion;
    }

    @JsonProperty("app_version")
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
