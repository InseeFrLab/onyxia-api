package fr.insee.onyxia.api.configuration.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.model.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:regions.json",factory = RegionsConfiguration.JsonLoader.class)
@ConfigurationProperties
public class RegionsConfiguration {

    private String regions;

    private Region[] resolvedRegions;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void debug() throws Exception {
        resolvedRegions = mapper.readValue(regions, Region[].class);
        System.out.println("Serving in "+resolvedRegions.length+" regions");
    }

    public Region getDefaultRegion() {
        return resolvedRegions[0];
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public Region[] getResolvedRegions() {
        return resolvedRegions;
    }

    public static class JsonLoader implements PropertySourceFactory {

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(String name,
                                                                                   EncodedResource resource) throws IOException {
            JsonNode values = new ObjectMapper().readTree(resource.getInputStream());
            return new MapPropertySource("regions-source", Map.of("regions",values.get("regions").toString()));
        }

    }
}
