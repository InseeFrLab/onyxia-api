package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaRegistryService {

    private static final String SCHEMA_DIRECTORY = "/schemas";
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemaRegistry;

    public JsonSchemaRegistryService() {
        this.objectMapper = new ObjectMapper();
        this.schemaRegistry = new HashMap<>();
    }

    @PostConstruct
    private void loadSchemas() throws IOException, URISyntaxException {
        Files.walk(Paths.get(JsonSchemaRegistryService.class.getResource(SCHEMA_DIRECTORY).toURI()))
                .filter(Files::isRegularFile)
                .forEach(this::loadSchema);
    }

    private void loadSchema(Path path) {
        try {
            JsonNode schema = objectMapper.readTree(path.toFile());
            schemaRegistry.put(path.getFileName().toString(), schema);
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
