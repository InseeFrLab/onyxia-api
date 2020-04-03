package fr.insee.onyxia.api.universe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.dao.universe.CatalogLoader;

@SpringBootTest
public class CatalogLoaderTest {

    @Autowired
    private CatalogLoader catalogLoader;

    @Test
    public void loadTest() {
        Assertions.assertNotNull(catalogLoader);
        CatalogWrapper uw = new CatalogWrapper();
        uw.setType("universe");
        uw.setLocation("classpath:universe-internal.json");
        catalogLoader.updateCatalog(uw);
        uw.getCatalog();
    }

}