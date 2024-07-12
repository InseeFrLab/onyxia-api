package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class JsonSchemaResolutionService {

    private final ObjectMapper objectMapper;
    private final JsonSchemaRegistryService registryService;

    @Autowired
    public JsonSchemaResolutionService(JsonSchemaRegistryService registryService) {
        this.objectMapper = new ObjectMapper();
        this.registryService = registryService;
    }

    public JsonNode resolveReferences(JsonNode schemaNode) {
        return resolveReferences(schemaNode, schemaNode);
    }

    private JsonNode resolveReferences(JsonNode schemaNode, JsonNode rootNode) {
        if (schemaNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) schemaNode;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals("$ref") && field.getValue().isTextual()) {
                    String ref = field.getValue().asText();
                    JsonNode refNode = null;
                    if (ref.startsWith("#/definitions/")) {
                        refNode = rootNode.at(ref.substring(1));
                    } else {
                        refNode = registryService.getSchema(ref);
                    }
                    if (refNode != null && !refNode.isMissingNode()) {
                        JsonNode resolvedNode = resolveReferences(refNode.deepCopy(), rootNode);
                        fields.remove();  // Remove the $ref field
                        objectNode.setAll((ObjectNode) resolvedNode);
                    }
                } else {
                    objectNode.set(field.getKey(), resolveReferences(field.getValue(), rootNode));
                }
            }
        } else if (schemaNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) schemaNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, resolveReferences(arrayNode.get(i), rootNode));
            }
        }
        return schemaNode;
    }
}
