package fr.insee.onyxia.model.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.insee.onyxia.model.helm.Repository;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest()
public class CatalogTest {
    @Autowired private ResourceLoader resourceLoader;

    @Test
    public void shouldReadHelm() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream =
                resourceLoader.getResource("classpath:index.yaml").getInputStream();
        Repository repo = mapper.readValue(inputStream, Repository.class);
        assertEquals(1, repo.getEntries().size());
    }

    @SpringBootApplication
    static class TestConfiguration {}
}
