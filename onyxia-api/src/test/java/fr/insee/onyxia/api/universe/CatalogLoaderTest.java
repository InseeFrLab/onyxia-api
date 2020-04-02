package fr.insee.onyxia.api.universe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.dao.universe.CatalogLoader;

@SpringBootTest
public class CatalogLoaderTest {

    @Autowired
    CatalogLoader catalogLoader;

    @Test
    public void loadTest() {
        CatalogWrapper uw = new CatalogWrapper();
        uw.setLocation("classpath:universe-internal.json");
        catalogLoader.updateCatalog(uw);
        uw.getCatalog();
    }

}