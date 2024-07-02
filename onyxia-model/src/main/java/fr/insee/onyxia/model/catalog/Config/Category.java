package fr.insee.onyxia.model.catalog.Config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;

/** Properties */
@Schema(description = "")
public class Category {

    @Schema(description = "")
    Map<String, Property> properties = new LinkedHashMap<>();

    @Schema(description = "")
    String description;

    @Schema(description = "")
    String type;

    @Schema(description = "")
    String[] required;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonAnyGetter
    public Map<String, Property> getProperties() {
        // Map<String,Property> map = new HashMap<>();
        // for (Entry<String,Property> entry : properties.entrySet()) {
        // //if (entry.getValue().getApiDefined() == null) {
        // map.put(entry.getKey(), entry.getValue());
        // }
        // }
        return properties;
    }

    // @Transient
    // public Map<String,Property> getAllProperties() {
    // return properties;
    // }
    public String[] getRequired() {
        return required;
    }

    public void setRequired(String[] required) {
        this.required = required;
    }

    @JsonAnySetter
    public void setUnrecognizedFields(String key, Property value) {
        this.properties.put(key, value);
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }
}
