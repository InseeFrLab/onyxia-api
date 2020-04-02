
package fr.insee.onyxia.model.helm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fr.insee.onyxia.model.catalog.Catalog;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Repository extends Catalog {

    @JsonProperty("apiVersion")
    private String apiVersion;
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

    @JsonProperty("entries")
    public void setEntries(Map<String, List<Chart>> entries) {
        setPackages(entries.values().stream().map(charts -> charts.get(0)).collect(Collectors.toList()));
    }
}
