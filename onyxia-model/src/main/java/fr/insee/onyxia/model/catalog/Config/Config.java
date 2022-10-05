package fr.insee.onyxia.model.catalog.Config;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Config
 */
@Schema(description = "")
public class Config {

    @Schema(description = "")
    private String type;
    @Schema(description = "")
    private Category properties;
    @Schema(description = "")
    private List<String> required = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Category getProperties() {
        return properties;
    }

    public void setProperties(Category properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

}