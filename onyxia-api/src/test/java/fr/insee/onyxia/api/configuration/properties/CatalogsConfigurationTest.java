package fr.insee.onyxia.api.configuration.properties;

import static fr.insee.onyxia.api.util.TestUtils.getClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.CustomObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogsConfigurationTest {

    @Test
    void shouldBeAbleToParseCatalogWithTimeout() throws Exception {
        ObjectMapper objectMapper = new CustomObjectMapper().objectMapper();
        String catalogWrapper = getClassPathResource("catalog-loader-test/catalog-wrapper.json5");

        CatalogsConfiguration catalogsConfiguration = new CatalogsConfiguration(objectMapper);
        catalogsConfiguration.setCatalogs(catalogWrapper);
        catalogsConfiguration.load();
        List<CatalogWrapper> resolvedCatalogs = catalogsConfiguration.getResolvedCatalogs();

        assertEquals(1, resolvedCatalogs.size());
        assertEquals("10m", resolvedCatalogs.getFirst().getTimeout());
    }
}
