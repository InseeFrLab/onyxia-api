package fr.insee.onyxia.api.dao.universe;

import fr.insee.onyxia.api.configuration.Catalogs;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CatalogRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRefresher.class);

    private final Catalogs catalogs;
    private final CatalogLoader catalogLoader;
    private final HelmRepoService helmRepoService;

    @Autowired
    public CatalogRefresher(
            Catalogs catalogs, CatalogLoader catalogLoader, HelmRepoService helmRepoService) {
        this.catalogs = catalogs;
        this.catalogLoader = catalogLoader;
        this.helmRepoService = helmRepoService;
    }

    private void refreshCatalogs() {
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

    @Scheduled(fixedDelayString = "${catalogs.refresh.ms}")
    public synchronized void run() {
        LOGGER.info("Refreshing catalogs");
        try {
            refresh();
        } catch (Exception e) {
            LOGGER.error("Catalog refreshing failed", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialRefresh() {
        run();
    }
}
