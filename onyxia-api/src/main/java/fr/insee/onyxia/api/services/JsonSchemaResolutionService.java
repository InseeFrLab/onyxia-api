package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
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
        List<String> roles = new ArrayList<>();
        return resolveReferences(schemaNode, schemaNode, roles);
    }

    public JsonNode resolveReferences(JsonNode schemaNode, List<String> roles) {
        return resolveReferences(schemaNode, schemaNode, roles);
    }

    private JsonNode resolveReferences(JsonNode schemaNode, JsonNode rootNode, List<String> roles) {
        return resolveReferences(schemaNode, rootNode, roles, null);
    }

    // Returns a fresh JsonNode by resolving all $ref, overwriteSchemaWith and patchSchemaWith
    // in schemaNode and then merging the result with patchNode (if not null)
    private JsonNode resolveReferences(
            JsonNode schemaNode, JsonNode rootNode, List<String> roles, JsonNode patchNode) {
        if (schemaNode.isObject()) {
            return resolveReferences((ObjectNode) schemaNode, rootNode, roles, patchNode);
        } else if (patchNode != null) {
            // If provided, the patch replaces any non-object
            return patchNode;
        } else if (schemaNode.isArray()) {
            ArrayNode arrayNode = this.objectMapper.createArrayNode();
            for (int i = 0; i < schemaNode.size(); i++) {
                arrayNode.set(i, resolveReferences(schemaNode.get(i), rootNode, roles));
            }
            return arrayNode;
        } else {
            return schemaNode;
        }
    }

    private JsonNode resolveReferences(
            ObjectNode schemaNode, JsonNode rootNode, List<String> roles, JsonNode patchNode) {
        if (patchNode != null && !patchNode.isObject()) {
            // If the patch is a value (not an object), then it replaces the schema
            return resolveReferences(patchNode, rootNode, roles, null);
        }

        // Special case 1: resolve $ref
        if (schemaNode.has("$ref")) {
            String ref = schemaNode.get("$ref").asText();
            JsonNode refNode;
            if (ref.startsWith("#/definitions/")) {
                refNode = rootNode.at(ref.substring(1));
            } else {
                refNode = registryService.getSchema(roles, ref);
            }
            if (refNode != null && !refNode.isMissingNode()) {
                // Proceed with the $ref node but still apply current patch (if any)
                return resolveReferences(refNode, rootNode, roles, patchNode);
            }
        }

        // Special case 2: resolve x-onyxia.overwriteSchemaWith
        if (schemaNode.has("x-onyxia") && schemaNode.get("x-onyxia").has("overwriteSchemaWith")) {
            String overwriteSchemaName =
                    schemaNode.get("x-onyxia").get("overwriteSchemaWith").asText();
            JsonNode overwriteSchemaNode = registryService.getSchema(roles, overwriteSchemaName);
            if (overwriteSchemaNode != null && !overwriteSchemaNode.isMissingNode()) {
                // Proceed with the overwriteSchemaWith node but still apply current patch (if any)
                return resolveReferences(overwriteSchemaNode, rootNode, roles, patchNode);
            }
        }

        // Special case 3: resolve x-onyxia.patchSchemaWith
        if (schemaNode.has("x-onyxia") && schemaNode.get("x-onyxia").has("patchSchemaWith")) {
            String patchSchemaName = schemaNode.get("x-onyxia").get("patchSchemaWith").asText();
            JsonNode newPatchNode = registryService.getSchema(roles, patchSchemaName);
            if (newPatchNode != null && !newPatchNode.isMissingNode()) {
                if (!newPatchNode.isObject()) {
                    // If the new patch is not an object, it replaces the schema
                    return resolveReferences(newPatchNode, rootNode, roles, patchNode);
                } else if (patchNode == null) {
                    // Otherwise it's an object. If no patch is currently applied, then it becomes
                    // the patch.
                    patchNode = newPatchNode;
                } else {
                    // Otherwise we have two object patches.
                    // Apply first the new patch to the schema object
                    schemaNode =
                            resolveReferencesInObject(
                                    schemaNode, rootNode, roles, (ObjectNode) newPatchNode);
                    // then proceed with the main processing and apply the original patch
                }
            }
        }
        return cleanOnyxiaTags(
                resolveReferencesInObject(schemaNode, rootNode, roles, (ObjectNode) patchNode));
    }

    private ObjectNode resolveReferencesInObject(
            ObjectNode schemaNode, JsonNode rootNode, List<String> roles, ObjectNode patchNode) {
        ObjectNode objectNode = this.objectMapper.createObjectNode();
        // Iterate over the schema keys
        Iterator<Map.Entry<String, JsonNode>> it = schemaNode.fields();

        if (patchNode == null) {
            // If no patch is supplied, simply map all keys from schemaNode to their resolved values
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                objectNode.put(
                        entry.getKey(), resolveReferences(entry.getValue(), rootNode, roles));
            }

        } else {
            //
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String key = entry.getKey();
                JsonNode patchVal = patchNode.get(key);
                if (patchVal == null) {
                    // The key is not patched: resolve the value without any patch
                    objectNode.put(key, resolveReferences(entry.getValue(), rootNode, roles));
                } else if (!patchVal.isNull()) {
                    // The key is patched to non-null: resolve the value using that patch
                    objectNode.put(
                            key, resolveReferences(entry.getValue(), rootNode, roles, patchVal));
                }
                // Otherwise the key is patched to an explicit null: to not add it to the output
                // schema (removed key)
            }

            // For all keys in the patch
            Iterator<Map.Entry<String, JsonNode>> patchIt = patchNode.fields();
            while (patchIt.hasNext()) {
                Map.Entry<String, JsonNode> patchEntry = patchIt.next();
                String patchKey = patchEntry.getKey();
                JsonNode patchVal = patchEntry.getValue();
                // If the key is patched with a non-null value, not already set in the previous
                // loop: add the patch value
                if (!patchVal.isNull() && !objectNode.has(patchKey)) {
                    objectNode.put(patchKey, patchVal);
                }
            }
        }
        return objectNode;
    }

    private ObjectNode cleanOnyxiaTags(ObjectNode node) {
        JsonNode onyxiaNode = node.get("x-onyxia");
        if (onyxiaNode != null && onyxiaNode.isObject()) {
            ObjectNode onyxiaObject = (ObjectNode) onyxiaNode;
            onyxiaObject.remove("patchSchemaWith");
            onyxiaObject.remove("overwriteSchemaWith");
            if (onyxiaObject.isEmpty()) {
                node.remove("x-onyxia");
            }
        }
        return node;
    }
}
