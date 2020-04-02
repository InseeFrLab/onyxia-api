package fr.insee.onyxia.api.dao.universe;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.catalog.Universe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;

@Service
public class CatalogLoader {

    private final Logger logger = LoggerFactory.getLogger(CatalogRefresher.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper mapper;

    public void updateCatalog(CatalogWrapper cw) {
        if (cw.getType().equals("universe")) {
            try {
                logger.info("updating catalog with id:" + cw.getId());
                Reader reader = new InputStreamReader(resourceLoader.getResource(cw.getLocation()).getInputStream(),
                        "UTF-8");
                cw.setCatalog(mapper.readValue(reader, Universe.class));
                cw.setLastUpdateTime(System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Property friendlyName = new Property();
        // friendlyName.setType("string");
        // friendlyName.setDescription("Nom d'affichage du service sur Onyxia");
        // friendlyName.setDefaut(pkg.getName());
        // friendlyName.setTitle("Un titre plus sympathique?");
        // Category onyxia = new Category();
        // Map<String, Property> map = new HashMap<String, Property>();
        // map.put("friendly_name", friendlyName);
        // onyxia.setProperties(map);
        // onyxia.setType("object");
        // onyxia.setDescription("Configure l'ensemble des metas-donnees pour Onyxia");
        // pkg.getProperties().getCategories().put("onyxia", onyxia);

    }

}