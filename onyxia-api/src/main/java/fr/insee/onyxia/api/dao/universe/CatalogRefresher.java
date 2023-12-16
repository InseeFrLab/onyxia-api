package fr.insee.onyxia.api.dao.universe;

import fr.insee.onyxia.api.configuration.Catalogs;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class CatalogRefresher implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(CatalogRefresher.class);

    @Autowired private Catalogs catalogs;

    @Autowired private CatalogLoader catalogLoader;

    @Value("${catalogs.refresh.ms}")
    private long refreshTime;

    @Autowired private HelmRepoService helmRepoService;

    private void refresh() {
        catalogs.getCatalogs().stream()
                .forEach(
                        c -> {
                            try {
                                logger.info(
                                        helmRepoService.addHelmRepo(
                                                c.getLocation(),
                                                c.getId(),
                                                c.getSkipTlsVerify(),
                                                c.getCaFile()));
                                catalogLoader.updateCatalog(c);
                            } catch (Exception e) {
                                logger.warn("Exception occurred", e);
                            }
                        });

        try {
            helmRepoService.repoUpdate();
        } catch (InterruptedException | TimeoutException | IOException e) {
            logger.warn("Exception occurred", e);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.refresh();
        if (refreshTime > 0L) {
            Timer timer = new Timer();
            TimerTask timerTask =
                    new TimerTask() {
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
