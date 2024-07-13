package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            Map<String, JsonNode> updates = new HashMap<>();

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
                        updates.putAll(convertToMap((ObjectNode) resolvedNode));
                        updates.put("$ref", null);
                    }
                } else {
                    updates.put(field.getKey(), resolveReferences(field.getValue(), rootNode));
                }
            }

            for (Map.Entry<String, JsonNode> update : updates.entrySet()) {
                if (update.getValue() == null) {
                    objectNode.remove(update.getKey());
                } else {
                    objectNode.set(update.getKey(), update.getValue());
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

    private Map<String, JsonNode> convertToMap(ObjectNode objectNode) {
        Map<String, JsonNode> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            map.put(field.getKey(), field.getValue());
        }
        return map;
    }
}
