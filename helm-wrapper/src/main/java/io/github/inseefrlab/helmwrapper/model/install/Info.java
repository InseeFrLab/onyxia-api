
package io.github.inseefrlab.helmwrapper.model.install;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "first_deployed", "last_deployed", "deleted", "description", "status", "notes" })
public class Info {

    @JsonProperty("first_deployed")
    private String firstDeployed;
    @JsonProperty("last_deployed")
    private String lastDeployed;
    @JsonProperty("deleted")
    private String deleted;
    @JsonProperty("description")
    private String description;
    @JsonProperty("status")
    private String status;
    @JsonProperty("notes")
    private String notes;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("first_deployed")
    public String getFirstDeployed() {
        return firstDeployed;
    }

    @JsonProperty("first_deployed")
    public void setFirstDeployed(String firstDeployed) {
        this.firstDeployed = firstDeployed;
    }

    @JsonProperty("last_deployed")
    public String getLastDeployed() {
        return lastDeployed;
    }

    @JsonProperty("last_deployed")
    public void setLastDeployed(String lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    @JsonProperty("deleted")
    public String getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(String notes) {
        this.notes = notes;
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
