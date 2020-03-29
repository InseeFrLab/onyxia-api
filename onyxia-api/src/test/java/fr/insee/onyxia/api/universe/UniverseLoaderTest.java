package fr.insee.onyxia.api.universe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.onyxia.api.configuration.UniverseWrapper;
import fr.insee.onyxia.api.dao.universe.UniverseLoader;

@SpringBootTest
public class UniverseLoaderTest {

    @Autowired
    UniverseLoader universeLoader;

    @Test
    public void loadTest() {
        UniverseWrapper uw = new UniverseWrapper();
        uw.setLocation("classpath:universe-cloudshell.json");
        universeLoader.updateUniverse(uw);
        uw.getUniverse();
    }


}