package fr.insee.onyxia.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * The schema registries are filled by a {@code @PostConstruct} method: these tests go through the
 * Spring context so that they fail if that lifecycle callback is not invoked (e.g. when it is
 * annotated with {@code javax.annotation.PostConstruct}, which Spring 7 ignores).
 */
@SpringBootTest(
        classes = {
            JsonSchemaResolutionService.class,
            JsonSchemaRegistryService.class,
            JsonSchemaProfileRegistryService.class
        })
class JsonSchemaResolutionServiceTest {

    @TempDir static Path profileDirectory;

    @DynamicPropertySource
    static void profileSchemaDirectory(DynamicPropertyRegistry registry) throws IOException {
        Files.writeString(
                profileDirectory.resolve("default"),
                """
                {"type": "object", "properties": {"email": {"type": "string"}}}
                """);
        registry.add("profile.schema.directory", profileDirectory::toString);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired JsonSchemaResolutionService resolutionService;

    @Autowired JsonSchemaRegistryService registryService;

    @Autowired JsonSchemaProfileRegistryService profileRegistryService;

    @Test
    void registryLoadsBundledSchemas() {
        assertFalse(registryService.listSchemas().isEmpty());
        assertNotNull(registryService.getSchema(List.of(), "ide/resources.json"));
        assertNotNull(registryService.getSchema(List.of(), "nodeSelector.json"));
    }

    @Test
    void overwriteSchemaWithIsReplacedByTheRegistrySchema() {
        JsonNode schema =
                mapper.readTree(
                        """
                        {
                          "type": "object",
                          "properties": {
                            "resources": {
                              "type": "object",
                              "x-onyxia": {"overwriteSchemaWith": "ide/resources.json"}
                            },
                            "nodeSelector": {
                              "type": "object",
                              "x-onyxia": {"overwriteSchemaWith": "nodeSelector.json"}
                            }
                          }
                        }
                        """);

        JsonNode resolved = resolutionService.resolveReferences(schema, List.of());

        JsonNode resources = resolved.at("/properties/resources");
        assertEquals("Resources", resources.path("title").asString());
        assertTrue(resources.at("/properties/requests/properties/cpu").isObject());
        assertTrue(resources.at("/x-onyxia/overwriteSchemaWith").isMissingNode());

        JsonNode nodeSelector = resolved.at("/properties/nodeSelector");
        assertEquals("Node Selector", nodeSelector.path("title").asString());
        assertEquals(
                "region.nodeSelector",
                nodeSelector.at("/x-onyxia/overwriteDefaultWith").asString());
    }

    @Test
    void internalReferencesAreResolved() {
        JsonNode schema =
                mapper.readTree(
                        """
                        {
                          "definitions": {"port": {"type": "integer", "default": 8080}},
                          "properties": {"port": {"$ref": "#/definitions/port"}}
                        }
                        """);

        JsonNode port =
                resolutionService.resolveReferences(schema, List.of()).at("/properties/port");

        assertEquals(8080, port.path("default").asInt());
        assertTrue(port.path("$ref").isMissingNode());
    }

    @Test
    void resolutionDoesNotMutateTheChartSchema() {
        JsonNode schema =
                mapper.readTree(
                        """
                        {"properties": {"resources": {"x-onyxia": {"overwriteSchemaWith": "ide/resources.json"}}}}
                        """);
        JsonNode original = schema.deepCopy();

        resolutionService.resolveReferences(schema, List.of());

        assertEquals(original, schema);
    }

    @Test
    void profileRegistryLoadsDefaultProfileSchema() {
        JsonNode profile = profileRegistryService.getProfileSchema();
        assertNotNull(profile);
        assertEquals("string", profile.at("/properties/email/type").asString());
    }
}
