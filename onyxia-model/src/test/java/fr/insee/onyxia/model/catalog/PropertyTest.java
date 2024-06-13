package fr.insee.onyxia.model.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.insee.onyxia.model.catalog.Config.Property;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropertyTest {

    private ObjectMapper mapper;
    private Property property;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        mapper.registerModule(module);

        property = new Property();
        property.setType("string");
        property.setDescription("A sample description");
        property.setTitle("A sample title");
        property.setDefaut("default value");
        property.setPattern(".*");
        property.setMinimum("1");
        property.setRender("render");
        property.setSliderMin(0);
        property.setSliderMax(100);
        property.setSliderStep(1);
        property.setSliderUnit("units");
        property.setSliderExtremity("extremity");
        property.setSliderExtremitySemantic("semantic");
        property.setSliderRangeId("rangeId");

        Property.XOnyxia xOnyxia = new Property.XOnyxia();
        xOnyxia.setOverwriteDefaultWith("user.email");
        xOnyxia.setOverwriteListEnumWith(Arrays.asList("value1", 2, 3.14));
        xOnyxia.setUseRegionSliderConfig("config");
        xOnyxia.setFormFieldLabel("label");
        xOnyxia.setFormFieldHelperText("helper text");
        property.setXonyxia(xOnyxia);
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        String json = mapper.writeValueAsString(property);
        System.out.println("Serialized JSON: \n" + json);
        assertTrue(json.contains("\"type\":\"string\""));
        assertTrue(json.contains("\"description\":\"A sample description\""));
        assertTrue(json.contains("\"title\":\"A sample title\""));
    }

    @Test
    public void testDeserialization() throws IOException {
        String json = mapper.writeValueAsString(property);
        Property deserializedProperty = mapper.readValue(json, Property.class);
        assertEquals(property.getType(), deserializedProperty.getType());
        assertEquals(property.getDescription(), deserializedProperty.getDescription());
        assertEquals(property.getTitle(), deserializedProperty.getTitle());
    }

    @Test
    public void testEdgeCases() throws JsonProcessingException, IOException {
        Property edgeCaseProperty = new Property();
        edgeCaseProperty.setXonyxia(new Property.XOnyxia());
        edgeCaseProperty.getXonyxia().setOverwriteListEnumWith(List.of());

        String json = mapper.writeValueAsString(edgeCaseProperty);
        Property deserializedEdgeCaseProperty = mapper.readValue(json, Property.class);

        assertTrue(deserializedEdgeCaseProperty.getXonyxia().getOverwriteListEnumWith().isEmpty());
    }

    @Test
    public void testOverwriteListEnumWithVariousTypes() throws JsonProcessingException, IOException {
        Property.XOnyxia xOnyxia = new Property.XOnyxia();
        xOnyxia.setOverwriteListEnumWith(Arrays.asList("USER_ONYXIA", "codegouv", "onyxia", "sspcloud-admin"));
        property.setXonyxia(xOnyxia);

        String json = mapper.writeValueAsString(property);
        Property deserializedProperty = mapper.readValue(json, Property.class);

        List<Object> overwriteListEnumWith = deserializedProperty.getXonyxia().getOverwriteListEnumWith();
        assertEquals(4, overwriteListEnumWith.size());
        assertTrue(overwriteListEnumWith.contains("USER_ONYXIA"));
        assertTrue(overwriteListEnumWith.contains("codegouv"));
        assertTrue(overwriteListEnumWith.contains("onyxia"));
        assertTrue(overwriteListEnumWith.contains("sspcloud-admin"));
    }
}
