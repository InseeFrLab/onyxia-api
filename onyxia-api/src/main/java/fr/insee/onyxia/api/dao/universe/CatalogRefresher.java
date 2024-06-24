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

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRefresher.class);

    private final Catalogs catalogs;
    private final CatalogLoader catalogLoader;
    private final long refreshTime;
    private final HelmRepoService helmRepoService;

    @Autowired
    public CatalogRefresher(
            Catalogs catalogs,
            CatalogLoader catalogLoader,
            HelmRepoService helmRepoService,
            @Value("${catalogs.refresh.ms}") long refreshTime) {
        this.catalogs = catalogs;
        this.catalogLoader = catalogLoader;
        this.helmRepoService = helmRepoService;
        this.refreshTime = refreshTime;
    }

    private void refreshCatalogs() throws InterruptedException {
        catalogs.getCatalogs()
                .forEach(
                        c -> {
                            try {
                                LOGGER.info("Adding Helm Repo: {}", c.getId());
                                helmRepoService.addHelmRepo(
                                        c.getLocation(),
                                        c.getId(),
                                        c.getSkipTlsVerify(),
                                        c.getCaFile());
                                LOGGER.info("Updating catalog: {}", c.getId());
                                catalogLoader.updateCatalog(c);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e); // or handle it as needed
                            } catch (Exception e) {
                                LOGGER.warn(
                                        "Exception occurred while updating catalog: {}",
                                        c.getId(),
                                        e);
                            }
                        });
    }

    private void updateRepo() throws InterruptedException {
        try {
            LOGGER.info("Updating Helm Repo Service...");
            helmRepoService.repoUpdate();
        } catch (InterruptedException e) {
            LOGGER.warn("InterruptedException occurred during repoUpdate", e);
            Thread.currentThread().interrupt();
            throw e;
        } catch (TimeoutException | IOException e) {
            LOGGER.warn("Exception occurred during repoUpdate", e);
        }
    }

    private void refresh() throws InterruptedException {
        updateRepo();
        refreshCatalogs();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Starting catalog refresher...");
        try {
            refresh();
        } catch (InterruptedException e) {
            LOGGER.warn("Run method interrupted", e);
            Thread.currentThread().interrupt();
        }

        if (refreshTime > 0L) {
            Timer timer = new Timer();
            TimerTask timerTask =
                    new TimerTask() {
                        @Override
                        public void run() {
                            LOGGER.info("Refreshing catalogs");
                            try {
                                refresh();
                            } catch (InterruptedException e) {
                                LOGGER.warn("Timer task interrupted", e);
                                Thread.currentThread().interrupt();
                            }
                        }
                    };
            timer.scheduleAtFixedRate(timerTask, refreshTime, refreshTime);
        }
    }
}
