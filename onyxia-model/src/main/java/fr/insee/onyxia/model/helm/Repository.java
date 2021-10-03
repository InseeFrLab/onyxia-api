
package fr.insee.onyxia.model.helm;

import com.fasterxml.jackson.annotation.*;
import fr.insee.onyxia.model.catalog.CatalogWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Repository extends CatalogWrapper {

    public static final String TYPE_HELM = "helm";

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
        setPackages(entries.values().stream().map(charts -> charts.get(0)).filter(chart -> "application".equalsIgnoreCase(chart.getType())).collect(Collectors.toList()));
    }
}
