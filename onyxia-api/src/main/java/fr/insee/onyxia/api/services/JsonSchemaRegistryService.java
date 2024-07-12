package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class JsonSchemaRegistryService {

    private static final String SCHEMA_DIRECTORY = "src/main/resources/schemas";
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemaRegistry;

    public JsonSchemaRegistryService() {
        this.objectMapper = new ObjectMapper();
        this.schemaRegistry = new HashMap<>();
    }

    @PostConstruct
    private void loadSchemas() throws IOException {
        Files.walk(Paths.get(SCHEMA_DIRECTORY))
                .filter(Files::isRegularFile)
                .forEach(this::loadSchema);
    }

    private void loadSchema(Path path) {
        try {
            JsonNode schema = objectMapper.readTree(path.toFile());
            String relativePath = Paths.get(SCHEMA_DIRECTORY).relativize(path).toString().replace("\\", "/");
            schemaRegistry.put(relativePath, schema);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema: " + path.getFileName(), e);
        }
    }

    public Map<String, JsonNode> listSchemas() {
        return schemaRegistry;
    }

    public JsonNode getSchema(String schemaName) {
        return schemaRegistry.get(schemaName);
    }
}
