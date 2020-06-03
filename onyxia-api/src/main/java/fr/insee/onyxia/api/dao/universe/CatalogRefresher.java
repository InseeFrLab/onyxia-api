package fr.insee.onyxia.api.dao.universe;

import fr.insee.onyxia.api.configuration.Catalogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class CatalogRefresher implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(CatalogRefresher.class);

    @Autowired
    private Catalogs catalogs;

    @Autowired
    private CatalogLoader catalogLoader;

    @Value("${catalogs.refresh.ms}")
    private long refreshTime;

    private void refresh() {
        catalogs.getCatalogs().parallelStream().forEach(c -> {
            try {
                catalogLoader.updateCatalog(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.refresh();
        if (refreshTime > 0L) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    logger.info("refreshing catalogs..");
                    refresh();
                }
            };
            timer.scheduleAtFixedRate(timerTask, refreshTime, refreshTime);
        }
    }
}
