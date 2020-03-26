package fr.insee.onyxia.api.universe;

import fr.insee.onyxia.api.configuration.Multiverse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootTest
@TestPropertySource(properties = {"universe.refresh.ms=3000"})
public class UniverseRefresherTest  {

    @Autowired
    private Multiverse multiverse;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void timeSchedulerCorrectlyInitialized() throws IOException {
        File tempFile = File.createTempFile("application-test","universeRefresher.json");
        Files.copy(resourceLoader.getResource("classpath:dummy-universe.json").getInputStream(), Paths.get("classpath:universe.json"), StandardCopyOption.REPLACE_EXISTING );

    } ;

}
