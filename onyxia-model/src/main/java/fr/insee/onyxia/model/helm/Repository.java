
package fr.insee.onyxia.model.helm;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "apiVersion", "entries", "generated" })
public class Repository {

    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("entries")
    private Entries entries;
    @JsonProperty("generated")
    private String generated;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @JsonProperty("entries")
    public Entries getEntries() {
        return entries;
    }

    @JsonProperty("entries")
    public void setEntries(Entries entries) {
        this.entries = entries;
    }

    @JsonProperty("generated")
    public String getGenerated() {
        return generated;
    }

    @JsonProperty("generated")
    public void setGenerated(String generated) {
        this.generated = generated;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    private class Entries {

        private Map<String, Chart> entrie = new HashMap<String, Chart>();

        @JsonAnyGetter
        public Map<String, Chart> getentrie() {
            return this.entrie;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Chart chart) {
            this.entrie.put(name, chart);
        }

    }
}
