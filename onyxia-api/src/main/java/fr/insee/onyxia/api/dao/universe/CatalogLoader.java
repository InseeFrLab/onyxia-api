package fr.insee.onyxia.api.dao.universe;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import java.util.List;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogLoader.class);

    @Autowired private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("helm")
    private ObjectMapper mapperHelm;

    public void updateCatalog(CatalogWrapper cw) {
        LOGGER.info("updating catalog with id :" + cw.getId() + " and type " + cw.getType());
        if (cw.getType().equals(Repository.TYPE_HELM)) {
            updateHelmRepository(cw);
        } else {
            LOGGER.warn("Unsupported catalog type: id: {}, type: {}", cw.getId(), cw.getType());
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
                            UTF_8);
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
                                                    } catch (CatalogLoaderException
                                                            | IOException e) {
                                                        LOGGER.info("Exception occurred", e);
                                                    }
                                                });
                            });
            repository.setPackages(
                    repository.getEntries().values().stream()
                            .map(List::getFirst)
                            .filter(chart -> "application".equalsIgnoreCase(chart.getType()))
                            .collect(Collectors.toList()));
            cw.setCatalog(repository);
            cw.setLastUpdateTime(System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.info("Exception occurred", e);
        }
    }

    private void refreshPackage(CatalogWrapper cw, Pkg pkg)
            throws CatalogLoaderException, IOException {
        if (!(pkg instanceof Chart chart)) {
            throw new IllegalArgumentException("Package should be of type Chart");
        }

        // TODO : support multiple urls
        Resource resource =
                resourceLoader
                        .getResource(cw.getLocation() + "/")
                        .createRelative(chart.getUrls().stream().findFirst().get());

        try (InputStream inputStream = resource.getInputStream()) {
            extractDataFromTgz(inputStream, chart);
        } catch (IOException e) {
            throw new CatalogLoaderException(
                    "Exception occurred during loading resource: " + resource.getDescription(), e);
        }
    }

    private void extractDataFromTgz(InputStream in, Chart chart) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        // HelmConfig config = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = tarIn.getNextEntry()) != null) {
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
            }
        }
    }
}
