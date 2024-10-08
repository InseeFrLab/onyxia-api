package fr.insee.onyxia.api.configuration.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.model.region.Region;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

@Configuration
@PropertySource(value = "classpath:regions.json", factory = RegionsConfiguration.JsonLoader.class)
@PropertySource(
        value = "classpath:regions-default.json",
        factory = RegionsConfiguration.JsonLoader.class,
        ignoreResourceNotFound = true)
@ConfigurationProperties
public class RegionsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegionsConfiguration.class);
    private String regions;
    private List<Region> resolvedRegions;
    private final ObjectMapper mapper;

    @Autowired
    public RegionsConfiguration(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void load() throws Exception {
        resolvedRegions = Arrays.asList(mapper.readValue(regions, Region[].class));
        resolvedRegions.forEach(
                region -> {
                    if (region.getServices()
                            .getAuthenticationMode()
                            .equals(Region.Services.AuthenticationMode.SERVICEACCOUNT)) {
                        LOGGER.warn(
                                "Using serviceAccount authentication for region {}. Onyxia will deploy services using it's own global permissions, this may be a security issue.",
                                region.getId());
                    }

                    if (region.getServices()
                            .getAuthenticationMode()
                            .equals(Region.Services.AuthenticationMode.IMPERSONATE)) {
                        LOGGER.info(
                                "Using impersonation authentication for region {}.",
                                region.getId());
                    }

                    if (region.getServices()
                            .getAuthenticationMode()
                            .equals(Region.Services.AuthenticationMode.TOKEN_PASSTHROUGH)) {
                        LOGGER.info(
                                "Using token passthrough authentication for region {}. User token will be used by Onyxia to interact with the API Server.",
                                region.getId());
                    }
                });
    }

    public Optional<Region> getRegionById(String regionId) {
        return resolvedRegions.stream()
                .filter(region -> region.getId().equals(regionId))
                .findFirst();
    }

    public Region getDefaultRegion() {
        return resolvedRegions.getFirst();
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public List<Region> getResolvedRegions() {
        return resolvedRegions;
    }

    public static class JsonLoader implements PropertySourceFactory {

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(
                String name, EncodedResource resource) throws IOException {
            JsonNode values = new ObjectMapper().readTree(resource.getInputStream());
            return new MapPropertySource(
                    "regions-source", Map.of("regions", values.get("regions").toString()));
        }
    }
}
