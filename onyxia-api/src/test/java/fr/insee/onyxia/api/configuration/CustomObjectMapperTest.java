package fr.insee.onyxia.api.configuration;

import static fr.insee.onyxia.api.util.TestUtils.getClassPathResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class CustomObjectMapperTest {
    CustomObjectMapper customObjectMapper = new CustomObjectMapper();

    @Test
    public void objectMapperShouldParseJson5IshDocument() {
        var mapper = customObjectMapper.objectMapper();
        var catalogsJson = getClassPathResource("catalog-loader-test/catalogs.json5");
        var catalogs = assertDoesNotThrow(() -> mapper.readValue(catalogsJson, Catalogs.class));
        assertThat(catalogs.getCatalogs().size(), equalTo(1));
    }
}
