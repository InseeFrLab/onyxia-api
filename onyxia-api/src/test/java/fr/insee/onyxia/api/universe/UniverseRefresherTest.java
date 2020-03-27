package fr.insee.onyxia.api.universe;

import fr.insee.onyxia.api.configuration.Multiverse;
import fr.insee.onyxia.api.configuration.UniverseWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@TestPropertySource(properties = {"universe.refresh.ms=3000"})
public class UniverseRefresherTest  {

    @Autowired
    private Multiverse multiverse;

    @Value("${universe.refresh.ms}")
    private long refreshTime;


    @Test
    public void timeSchedulerCorrectlyInitializedTest() throws InterruptedException {
        List<Long> timesBeforeListUpdate =generateTimeLists(multiverse);
        Thread.sleep(refreshTime*2);
        List<Long> timesAfterListUpdate = generateTimeLists(multiverse);
        Assertions.assertNotEquals(timesBeforeListUpdate,timesAfterListUpdate);
        } ;

    private List<Long> generateTimeLists(Multiverse multiverse){
        return multiverse.getUniverses()
                .stream()
                .map(UniverseWrapper::getLastUpdateTime)
                .collect(Collectors.toList());
    }


}
