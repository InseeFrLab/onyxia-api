package fr.insee.onyxia.api.universe;

import fr.insee.onyxia.api.configuration.Multiverse;
import fr.insee.onyxia.api.configuration.UniverseWrapper;
import fr.insee.onyxia.api.dao.universe.UniverseRefresher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {"universe.refresh.ms=3000","multiverse.configuration=classpath:multiverse.json","dummy-multiverse.configuration=classpath:dummy-multiverse.json"})
public class UniverseRefresherTest  {

    @Autowired
    private Multiverse multiverse;

    @Autowired
    private UniverseRefresher universeRefresher;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${universe.refresh.ms}")
    private long refreshTime;

    @Value("${multiverse.configuration}")
    private String multiverseConf;

    @Value("${dummy-multiverse.configuration}")
    private String dummyMultiverseConf;

    @Test
    public void timeSchedulerCorrectlyInitializedTest() throws IOException, InterruptedException {
        Path dummyMultiverseConfPath = resourceLoader.getResource(dummyMultiverseConf).getFile().toPath();
        Path multiVersePath = resourceLoader.getResource(multiverseConf).getFile().toPath();
        Files.copy(dummyMultiverseConfPath,multiVersePath, StandardCopyOption.REPLACE_EXISTING);
        List<UniverseWrapper> multiverseWrappers = multiverse.getUniverses();
        Thread.sleep(refreshTime);
        Assertions.assertNotEquals(multiverseWrappers.toString(),multiverse.getUniverses().toString());
        } ;


}
