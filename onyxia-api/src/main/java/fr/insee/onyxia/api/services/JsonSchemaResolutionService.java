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
        return resolveReferences(schemaNode, schemaNode, "");
    }

    public JsonNode resolveReferences(JsonNode schemaNode, String role) {
        return resolveReferences(schemaNode, schemaNode, role);
    }

    private JsonNode resolveReferences(JsonNode schemaNode, JsonNode rootNode, String role) {
        if (schemaNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) schemaNode;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            Map<String, JsonNode> updates = new HashMap<>();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode fieldValue = field.getValue();

                if (field.getKey().equals("$ref") && fieldValue.isTextual()) {
                    String ref = fieldValue.asText();
                    JsonNode refNode = null;
                    if (ref.startsWith("#/definitions/")) {
                        refNode = rootNode.at(ref.substring(1));
                    } else {
                        refNode = registryService.getSchema(role,ref);
                    }

                    if (refNode != null && !refNode.isMissingNode()) {
                        JsonNode resolvedNode = resolveReferences(refNode.deepCopy(), rootNode, role);
                        updates.putAll(convertToMap((ObjectNode) resolvedNode));
                        updates.put("$ref", null);
                    }
                } else if (fieldValue.isObject()
                        && fieldValue.has("x-onyxia")
                        && fieldValue.get("x-onyxia").has("overwriteSchemaWith")) {
                    String overrideSchemaName =
                            fieldValue.get("x-onyxia").get("overwriteSchemaWith").asText();
                    JsonNode overrideSchemaNode = registryService.getSchema(role,overrideSchemaName);

                    if (overrideSchemaNode != null && !overrideSchemaNode.isMissingNode()) {
                        JsonNode resolvedNode =
                                resolveReferences(overrideSchemaNode.deepCopy(), rootNode, role);
                        updates.put(field.getKey(), resolvedNode);
                    }
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    updates.put(field.getKey(), resolveReferences(fieldValue, rootNode, role));
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
                arrayNode.set(i, resolveReferences(arrayNode.get(i), rootNode, role));
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
