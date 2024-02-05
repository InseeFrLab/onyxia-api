package fr.insee.onyxia.api.configuration.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

@Configuration
@PropertySource(value = "classpath:catalogs.json", factory = CatalogsConfiguration.JsonLoader.class)
@PropertySource(
        value = "classpath:catalogs-default.json",
        factory = CatalogsConfiguration.JsonLoader.class,
        ignoreResourceNotFound = true)
@ConfigurationProperties
public class CatalogsConfiguration {

    private String catalogs;

    private List<CatalogWrapper> resolvedCatalogs;

    private ObjectMapper mapper;

    @Autowired
    public CatalogsConfiguration(ObjectMapper mapper) {
        mapper = this.mapper;
    }

    @PostConstruct
    public void load() throws Exception {
        resolvedCatalogs = Arrays.asList(mapper.readValue(catalogs, CatalogWrapper[].class));
    }

    public List<CatalogWrapper> getResolvedCatalogs() {
        return resolvedCatalogs;
    }

    public String getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(String catalogs) {
        this.catalogs = catalogs;
    }

    public void setResolvedCatalogs(List<CatalogWrapper> resolvedCatalogs) {
        this.resolvedCatalogs = resolvedCatalogs;
    }

    public static class JsonLoader implements PropertySourceFactory {

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(
                String name, EncodedResource resource) throws IOException {
            JsonNode values = new ObjectMapper().readTree(resource.getInputStream());
            return new MapPropertySource(
                    "catalogs-source", Map.of("catalogs", values.get("catalogs").toString()));
        }
    }
}
