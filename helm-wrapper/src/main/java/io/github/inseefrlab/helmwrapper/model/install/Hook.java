
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
@JsonPropertyOrder({ "name", "kind", "path", "manifest", "events", "last_run" })
public class Hook {

    @JsonProperty("name")
    private String name;
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("path")
    private String path;
    @JsonProperty("manifest")
    private String manifest;
    @JsonProperty("events")
    private List<String> events = null;
    @JsonProperty("last_run")
    private LastRun lastRun;
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

    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    @JsonProperty("kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("manifest")
    public String getManifest() {
        return manifest;
    }

    @JsonProperty("manifest")
    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    @JsonProperty("events")
    public List<String> getEvents() {
        return events;
    }

    @JsonProperty("events")
    public void setEvents(List<String> events) {
        this.events = events;
    }

    @JsonProperty("last_run")
    public LastRun getLastRun() {
        return lastRun;
    }

    @JsonProperty("last_run")
    public void setLastRun(LastRun lastRun) {
        this.lastRun = lastRun;
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
