package fr.insee.onyxia.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaProfileRegistryService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JsonSchemaProfileRegistryService.class);

    @Value("${profile.schema.directory:/userProfile}") // External directory path
    private String userProfileDirectory;

    private JsonNode defaultProfileSchema;

    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonSchemaProfileRegistryService() {}

    public JsonNode getProfileSchema() {
        return defaultProfileSchema;
    }

    @PostConstruct
    private void loadProfileSchema() {
        Path roleSchemaPath = Paths.get(userProfileDirectory);
        if (Files.exists(roleSchemaPath) && Files.exists(roleSchemaPath.resolve("default"))) {
            try {
                JsonNode schema = objectMapper.readTree(roleSchemaPath.resolve("default").toFile());
                defaultProfileSchema = schema;
            } catch (IOException e) {
                LOGGER.error("User profile : failed to load default profile", e);
            }
        }
    }
}
