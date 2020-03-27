package fr.insee.onyxia.api.dao.universe;

import fr.insee.onyxia.api.configuration.Multiverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class UniverseRefresher {

    private final Logger logger = LoggerFactory.getLogger(UniverseRefresher.class);

    @Autowired
    private Multiverse multiverse;

    @Autowired
    private UniverseLoader universeLoader;

    @Value("${universe.refresh.ms}")
    private long refreshTime;

    private void refresh(){
        multiverse.getUniverses().parallelStream().forEach(u -> universeLoader.updateUniverse(u));
    }

    @PostConstruct
    public void scheduleRefresher() {
        this.refresh();
        if (refreshTime > 0L) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    logger.info("refreshing universes..");
                    refresh();
                }
            };
            timer.scheduleAtFixedRate(timerTask,
                    refreshTime, refreshTime);
        }
    }

}
