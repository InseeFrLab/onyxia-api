package fr.insee.onyxia.api.universe;

        import fr.insee.onyxia.api.configuration.Multiverse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@SpringBootTest
@TestPropertySource(properties = {"universe.refresh.ms=3000","multiverse.configuration=classpath:multiverse.json","dummy-multiverse.configuration=classpath:multiverse.json"})
public class UniverseRefresherTest  {

    @Autowired
    private Multiverse multiverse;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${multiverse.configuration}")
    private String multiverseConf;

    @Value("${dummy-multiverse.configuration}")
    private String dummyMultiverseConf;

    @Test
    public void timeSchedulerCorrectlyInitializedTest() throws IOException, InterruptedException {
        Files.copy(resourceLoader.getResource(dummyMultiverseConf).getInputStream(), resourceLoader.getResource(multiverseConf).getFile().toPath(), StandardCopyOption.REPLACE_EXISTING );
    } ;

}
