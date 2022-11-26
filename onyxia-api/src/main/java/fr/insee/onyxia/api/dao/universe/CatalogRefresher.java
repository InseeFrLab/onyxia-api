package fr.insee.onyxia.api.dao.universe;

import fr.insee.onyxia.api.configuration.Catalogs;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
                                        helmRepoService.addHelmRepo(c.getLocation(), c.getId()));
                                catalogLoader.updateCatalog(c);
                                if (c.getCatalog() != null
                                        && !CollectionUtils.isEmpty(c.getCatalog().getPackages())) {
                                    c.getCatalog()
                                            .setPackages(
                                                    c.getCatalog().getPackages().stream()
                                                            .filter(
                                                                    pkg -> {
                                                                        if (c.getExcludedCharts()
                                                                                .contains(
                                                                                        pkg
                                                                                                .getName())) {
                                                                            logger.info(
                                                                                    "Ignoring chart "
                                                                                            + pkg
                                                                                                    .getName()
                                                                                            + " in catalog "
                                                                                            + c
                                                                                                    .getName());
                                                                            return false;
                                                                        }
                                                                        return true;
                                                                    })
                                                            .collect(Collectors.toList()));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

        try {
            helmRepoService.repoUpdate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
