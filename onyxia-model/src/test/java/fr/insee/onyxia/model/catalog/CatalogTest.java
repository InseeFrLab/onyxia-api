package fr.insee.onyxia.model.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.onyxia.model.helm.Repository;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

@SpringBootTest()
public class CatalogTest {
    @Autowired private ResourceLoader resourceLoader;

    @Test
    public void shouldReadHelm() throws IOException {
        ObjectMapper mapper =
                YAMLMapper.builder()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build();
        InputStream inputStream =
                resourceLoader.getResource("classpath:index.yaml").getInputStream();
        Repository repo = mapper.readValue(inputStream, Repository.class);
        assertEquals(1, repo.getEntries().size());
    }

    @SpringBootApplication
    static class TestConfiguration {}
}
