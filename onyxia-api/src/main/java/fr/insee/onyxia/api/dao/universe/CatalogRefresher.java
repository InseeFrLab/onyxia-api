package fr.insee.onyxia.api.dao.universe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.configuration.Catalogs;

@Service
public class CatalogRefresher {

    private final Logger logger = LoggerFactory.getLogger(CatalogRefresher.class);

    @Autowired
    private Catalogs catalogs;

    @Autowired
    private List<CatalogLoader> catalogLoaders;

    @Value("${universe.refresh.ms}")
    private long refreshTime;

    private void refresh() {
        catalogs.getCatalogs().parallelStream()
                .forEach(c -> catalogLoaders.stream().forEach(cl -> cl.updateCatalog(c)));
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
            timer.scheduleAtFixedRate(timerTask, refreshTime, refreshTime);
        }
    }

}
