package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaRegistryService {

    private static final String SCHEMA_DIRECTORY = "/schemas"; // Resource directory
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemaRegistry;
    // Role-based schema registry (Map of Maps)
    private final Map<String, Map<String, JsonNode>> roleSchemaRegistry;

    @Value("${external.schema.directory:/external-schemas}") // External directory path
    private String externalSchemaDirectory;

    @Value("${role.schema.directory:/role-schemas}") // External directory for role-based schemas
    private String roleSchemaDirectory;

    public JsonSchemaRegistryService() {
        this.objectMapper = new ObjectMapper();
        this.schemaRegistry = new HashMap<>();
        this.roleSchemaRegistry = new HashMap<>();
    }

    @PostConstruct
    private void loadSchemas() throws IOException, URISyntaxException {
        // Load initial schemas from resources
        loadResourceSchemas();

        // Load schemas from the external directory if it exists
        loadExternalSchemas();

        // Load role-based schemas from the role schema directory
        loadRoleSchemas();
    }

    private void loadResourceSchemas() throws IOException, URISyntaxException {
        Path resourcePath =
                Paths.get(JsonSchemaRegistryService.class.getResource(SCHEMA_DIRECTORY).toURI());
        Files.walk(resourcePath)
                .filter(Files::isRegularFile)
                .forEach(path -> loadSchema(resourcePath, path));
    }

    private void loadExternalSchemas() throws IOException {
        Path externalPath = Paths.get(externalSchemaDirectory);
        if (Files.exists(externalPath)) {
            Files.walk(externalPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> loadSchema(externalPath, path));
        }
    }

    private void loadRoleSchemas() throws IOException {
        Path roleSchemaPath = Paths.get(roleSchemaDirectory);
        if (Files.exists(roleSchemaPath)) {
            Files.walk(roleSchemaPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> loadRoleSchema(roleSchemaPath, path));
        }
    }

    private void loadSchema(Path basePath, Path path) {
        try {
            JsonNode schema = objectMapper.readTree(path.toFile());
            String key = generateKey(basePath, path);
            schemaRegistry.put(key, schema);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema: " + path.getFileName(), e);
        }
    }

    private String generateKey(Path basePath, Path path) {
        // Remove the base directory part from the path and replace the file separators with dots
        Path relativePath = basePath.relativize(path);
        return relativePath.toString().replace(File.separatorChar, '/');
    }

    // Load role-specific schemas into the role-based registry
    private void loadRoleSchema(Path basePath, Path schemaFile) {
        try {
            JsonNode schema = objectMapper.readTree(schemaFile.toFile());

            // Extract the role from the directory structure (first subdirectory under base path)
            Path relativePath = basePath.relativize(schemaFile);
            String role = relativePath.getName(0).toString(); // The first part of the relative path is the role

            // Generate the schema key based on the path after the role directory
            String schemaKey = relativePath.subpath(1, relativePath.getNameCount()).toString().replace(File.separatorChar, '/');

            // Add the schema to the role-specific registry
            roleSchemaRegistry
                .computeIfAbsent(role, k -> new HashMap<>())  // Ensure a map exists for the role
                .put(schemaKey, schema);  // Add the schema under the role

        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema for role from file: " + schemaFile.getFileName(), e);
        }
    }

    public Map<String, JsonNode> listSchemas() {
        return new HashMap<>(schemaRegistry);
    }

    public Map<String, JsonNode> listSchemas(String role) {
        // Create a new map starting with the default schemas
        Map<String, JsonNode> combinedSchemas = new HashMap<>(schemaRegistry);

        // Get role-specific schemas
        Map<String, JsonNode> roleSchemas = roleSchemaRegistry.getOrDefault(role, new HashMap<>());

        // Overwrite default schemas with role-specific schemas (if any)
        combinedSchemas.putAll(roleSchemas);

        // Return the combined map, with role schemas taking precedence
        return combinedSchemas;
    }

    public JsonNode getSchema(String schemaName) {
        return schemaRegistry.get(schemaName);
    }

    public JsonNode getSchema(String role, String schemaName) {
        // First, check if the schema exists in the role-based registry
        Map<String, JsonNode> roleSchemas = roleSchemaRegistry.get(role);
        if (roleSchemas != null && roleSchemas.containsKey(schemaName)) {
            return roleSchemas.get(schemaName); // Return the role-specific schema if found
        }
        // If not found in the role registry, return the default schema
        return getSchema(schemaName);
    }
}
