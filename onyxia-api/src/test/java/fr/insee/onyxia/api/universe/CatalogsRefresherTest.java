package fr.insee.onyxia.api.universe;

import fr.insee.onyxia.api.configuration.Catalogs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@TestPropertySource(properties = { "universe.refresh.ms=3000" })
public class CatalogsRefresherTest {

    @Autowired
    private Catalogs catalogs;

    @Value("${universe.refresh.ms}")
    private long refreshTime;

    @Test
    public void timeSchedulerCorrectlyInitializedTest() throws InterruptedException {
        List<Long> timesBeforeListUpdate = generateTimeLists(catalogs);
        Thread.sleep(refreshTime * 2);
        List<Long> timesAfterListUpdate = generateTimeLists(catalogs);
        Assertions.assertNotEquals(timesBeforeListUpdate, timesAfterListUpdate);
    };

    private List<Long> generateTimeLists(Catalogs catalogs) {
        return catalogs.getCatalogs().stream().map(c -> c.getLastUpdateTime()).collect(Collectors.toList());
    }

}
