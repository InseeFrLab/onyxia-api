
package io.github.inseefrlab.helmwrapper.model.install;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "info", "chart", "manifest", "hooks", "version", "namespace" })
public class HelmInstaller {

    @JsonProperty("name")
    private String name;
    @JsonProperty("info")
    private Info info;
    @JsonProperty("chart")
    private Chart chart;
    @JsonProperty("manifest")
    private String manifest;
    @JsonProperty("hooks")
    private List<Hook> hooks = null;
    @JsonProperty("version")
    private Integer version;
    @JsonProperty("namespace")
    private String namespace;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("info")
    public Info getInfo() {
        return info;
    }

    @JsonProperty("info")
    public void setInfo(Info info) {
        this.info = info;
    }

    @JsonProperty("chart")
    public Chart getChart() {
        return chart;
    }

    @JsonProperty("chart")
    public void setChart(Chart chart) {
        this.chart = chart;
    }

    @JsonProperty("manifest")
    public String getManifest() {
        return manifest;
    }

    @JsonProperty("manifest")
    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    @JsonProperty("hooks")
    public List<Hook> getHooks() {
        return hooks;
    }

    @JsonProperty("hooks")
    public void setHooks(List<Hook> hooks) {
        this.hooks = hooks;
    }

    @JsonProperty("version")
    public Integer getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Integer version) {
        this.version = version;
    }

    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
