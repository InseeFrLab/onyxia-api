package fr.insee.onyxia.model.catalog;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")
public class Pkg {
    @Schema(description = "")
    private String name;

    @Schema(description = "")
    private String description;

    @Schema(description = "")
    private String version;

    @Schema(description = "")
    private JsonNode schema;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pkg name(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pkg description(String description) {
        this.description = description;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JsonNode getSchema() {
        return this.schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }
    public Pkg version(String version) {
        this.version = version;
        return this;
    }
}
