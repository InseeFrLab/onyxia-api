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

    @Value("${external.schema.directory:/external-schemas}") // External directory path
    private String externalSchemaDirectory;

    public JsonSchemaRegistryService() {
        this.objectMapper = new ObjectMapper();
        this.schemaRegistry = new HashMap<>();
    }

    @PostConstruct
    private void loadSchemas() throws IOException, URISyntaxException {
        // Load initial schemas from resources
        loadResourceSchemas();

        // Load schemas from the external directory if it exists
        loadExternalSchemas();
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

    public void refreshExternalSchemas() throws IOException {
        loadExternalSchemas();
    }

    public Map<String, JsonNode> listSchemas() {
        return new HashMap<>(schemaRegistry);
    }

    public JsonNode getSchema(String schemaName) {
        return schemaRegistry.get(schemaName);
    }

    public void overwriteSchema(String schemaName, JsonNode newSchema) {
        schemaRegistry.put(schemaName, newSchema);
    }
}
