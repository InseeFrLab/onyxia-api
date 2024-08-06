package fr.insee.onyxia.api.controller.pub;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.onyxia.api.controller.exception.SchemaNotFoundException;
import fr.insee.onyxia.api.services.JsonSchemaRegistryService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/schemas")
public class JsonSchemaController {

    private final JsonSchemaRegistryService jsonSchemaRegistryService;

    @Autowired
    public JsonSchemaController(JsonSchemaRegistryService jsonSchemaRegistryService) {
        this.jsonSchemaRegistryService = jsonSchemaRegistryService;
    }

    @GetMapping
    public Map<String, JsonNode> listSchemas() {
        return jsonSchemaRegistryService.listSchemas();
    }

    @GetMapping("/{schemaName}")
    public JsonNode getSchema(@PathVariable String schemaName) {
        JsonNode schema = jsonSchemaRegistryService.getSchema(schemaName);
        if (schema == null) {
            throw new SchemaNotFoundException(schemaName);
        }
        return schema;
    }
}
