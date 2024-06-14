package fr.insee.onyxia.model.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.insee.onyxia.model.catalog.Config.Property;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertyTest {

    private ObjectMapper mapper;
    private Property property;

    @BeforeEach
    void setUp() {
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
        xOnyxia.setOverwriteListEnumWith("user.decodedIdToken.groups");
        xOnyxia.setUseRegionSliderConfig("config");
        xOnyxia.setFormFieldLabel("label");
        xOnyxia.setFormFieldHelperText("helper text");
        property.setXonyxia(xOnyxia);
    }

    @Test
    void testSerialization() throws IOException {
        String json = mapper.writeValueAsString(property);
        System.out.println("Serialized JSON: \n" + json);
        assertTrue(json.contains("\"type\":\"string\""));
        assertTrue(json.contains("\"description\":\"A sample description\""));
        assertTrue(json.contains("\"title\":\"A sample title\""));
    }

    @Test
    void testDeserialization() throws IOException {
        String json = mapper.writeValueAsString(property);
        Property deserializedProperty = mapper.readValue(json, Property.class);
        assertEquals(property.getType(), deserializedProperty.getType());
        assertEquals(property.getDescription(), deserializedProperty.getDescription());
        assertEquals(property.getTitle(), deserializedProperty.getTitle());
    }

    @Test
    void testListEnumProperty() throws IOException {
        Property pullPolicyProperty = new Property();
        pullPolicyProperty.setType("string");
        pullPolicyProperty.setDefaut("IfNotPresent");
        pullPolicyProperty.setListEnumeration(Arrays.asList("IfNotPresent", "Always", "Never"));

        String json = mapper.writeValueAsString(pullPolicyProperty);
        Property deserializedProperty = mapper.readValue(json, Property.class);

        assertNotNull(deserializedProperty, "PullPolicy property should not be null");
        assertEquals("IfNotPresent", deserializedProperty.getDefaut());

        List<Object> listEnum = (List<Object>) deserializedProperty.getListEnumeration();
        assertNotNull(listEnum, "listEnum should not be null");
        assertEquals(Arrays.asList("IfNotPresent", "Always", "Never"), listEnum);
    }

    @Test
    void testXOnyxiaProperty() throws IOException {
        Property groupProperty = new Property();
        groupProperty.setType("string");
        groupProperty.setDefaut("");
        groupProperty.setListEnumeration(List.of(""));

        Property.XOnyxia xOnyxia = new Property.XOnyxia();
        xOnyxia.setOverwriteDefaultWith("user.decodedIdToken.groups[0]");
        xOnyxia.setOverwriteListEnumWith("user.decodedIdToken.groups");
        groupProperty.setXonyxia(xOnyxia);

        // Serialize and deserialize to test
        String json = mapper.writeValueAsString(groupProperty);
        Property deserializedGroupProperty = mapper.readValue(json, Property.class);

        assertNotNull(deserializedGroupProperty, "Group property should not be null");
        assertEquals("", deserializedGroupProperty.getDefaut());

        List<Object> listEnum = (List<Object>) deserializedGroupProperty.getListEnumeration();
        assertNotNull(listEnum, "listEnum should not be null");
        assertEquals(List.of(""), listEnum);

        // Check x-onyxia values
        Property.XOnyxia deserializedXOnyxia = deserializedGroupProperty.getXonyxia();
        assertNotNull(deserializedXOnyxia, "XOnyxia property should not be null");
        assertEquals(
                "user.decodedIdToken.groups[0]", deserializedXOnyxia.getOverwriteDefaultWith());
        assertEquals("user.decodedIdToken.groups", deserializedXOnyxia.getOverwriteListEnumWith());
    }
}
