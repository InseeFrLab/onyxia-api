package fr.insee.onyxia.api.dao.universe;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.catalog.Config.Config;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.helm.Repository;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Collectors;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class CatalogLoader {

    private final Logger logger = LoggerFactory.getLogger(CatalogLoader.class);

    @Autowired private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("helm")
    private ObjectMapper mapperHelm;

    public void updateCatalog(CatalogWrapper cw) {
        logger.info("updating catalog with id :" + cw.getId() + " and type " + cw.getType());
        switch (cw.getType()) {
            case Repository.TYPE_HELM:
                updateHelmRepository(cw);
                break;
        }
    }

    /** TODO : move this helm specific logic somewhere else ? */
    private void updateHelmRepository(CatalogWrapper cw) {
        try {
            Reader reader =
                    new InputStreamReader(
                            resourceLoader
                                    .getResource(cw.getLocation() + "/index.yaml")
                                    .getInputStream(),
                            "UTF-8");
            Repository repository = mapperHelm.readValue(reader, Repository.class);
            repository
                    .getEntries()
                    .entrySet()
                    .removeIf(
                            entry ->
                                    cw.getExcludedCharts().stream()
                                            .anyMatch(
                                                    excludedChart ->
                                                            excludedChart.equalsIgnoreCase(
                                                                    entry.getKey())));
            repository.getEntries().values().parallelStream()
                    .forEach(
                            entry -> {
                                entry.parallelStream()
                                        .forEach(
                                                pkg -> {
                                                    try {
                                                        refreshPackage(cw, pkg);
                                                    } catch (CatalogLoaderException | IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                            });
            repository.setPackages(
                    repository.getEntries().values().stream()
                            .map(charts -> charts.get(0))
                            .filter(chart -> "application".equalsIgnoreCase(chart.getType()))
                            .collect(Collectors.toList()));
            cw.setCatalog(repository);
            cw.setLastUpdateTime(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshPackage(CatalogWrapper cw, Pkg pkg) throws CatalogLoaderException, IOException {
        if (!(pkg instanceof Chart chart)) {
            throw new IllegalArgumentException("Package should be of type Chart");
        }

        // TODO : support multiple urls
        Resource resource = resourceLoader
                .getResource(cw.getLocation() + "/index.yaml")
                .createRelative(chart.getUrls().stream().findFirst().get());

        try (InputStream inputStream = resource.getInputStream()) {
            extractDataFromTgz(inputStream, chart);
        } catch (IOException e) {
            throw new CatalogLoaderException("Exception occurred during loading resource: " + resource.getDescription(), e);
        }
    }

    private void extractDataFromTgz(InputStream in, Chart chart) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        // HelmConfig config = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.getName().endsWith(chart.getName() + "/values.schema.json")
                        && !entry.getName()
                                .endsWith("charts/" + chart.getName() + "/values.schema.json")) {
                    // TODO : mutualize objectmapper
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    Config config = mapper.readValue(tarIn, Config.class);
                    chart.setConfig(config);
                }

                if (entry.isDirectory()) {

                } else {

                }
            }
        }
    }
}
