package fr.insee.onyxia.api.dao.universe;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.helm.Repository;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CatalogLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogLoader.class);

    private final ResourceLoader resourceLoader;

    private final ObjectMapper mapperHelm;

    public CatalogLoader(
            ResourceLoader resourceLoader, @Qualifier("helm") ObjectMapper mapperHelm) {
        this.resourceLoader = resourceLoader;
        this.mapperHelm = mapperHelm;
    }

    public void updateCatalog(CatalogWrapper cw) {
        LOGGER.info("updating catalog with id :{} and type {}", cw.getId(), cw.getType());
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
            // Remove excluded services from list
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
            // For each service, filter the multiple versions if needed then refresh remaining
            // versions
            repository.getEntries().values().parallelStream()
                    .forEach(
                            charts -> {
                                epurateChartsList(charts, cw);
                                refreshChartsList(charts, cw);
                            });
            cw.setCatalog(repository);
            cw.setLastUpdateTime(System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.info("Exception occurred", e);
        }
    }

    private void epurateChartsList(List<Chart> charts, CatalogWrapper cw) {
        if (cw.getMultipleServicesMode() == CatalogWrapper.MultipleServicesMode.ALL) {
            return;
        }
        int i = 0;
        Optional<Version> previousVersion = Optional.empty();
        Iterator<Chart> iterator = charts.iterator();
        while (iterator.hasNext()) {
            Chart chart = iterator.next();
            if (cw.getMultipleServicesMode() == CatalogWrapper.MultipleServicesMode.LATEST
                    && i > 0) {
                iterator.remove();
            } else if (cw.getMultipleServicesMode()
                            == CatalogWrapper.MultipleServicesMode.MAX_NUMBER
                    && i >= cw.getMaxNumberOfVersions()) {
                iterator.remove();
            } else if (cw.getMultipleServicesMode()
                    == CatalogWrapper.MultipleServicesMode.SKIP_PATCHES) {
                Optional<Version> version = Version.tryParse(chart.getVersion());
                if (version.isPresent()
                        && previousVersion.isPresent()
                        && versionsAreSameMajorAndMinor(version.get(), previousVersion.get())) {
                    iterator.remove();
                }
                previousVersion = version;
            }

            i++;
        }
    }

    private boolean versionsAreSameMajorAndMinor(Version version, Version previousVersion) {
        return version.isSameMajorVersionAs(previousVersion)
                && version.isSameMinorVersionAs(previousVersion);
    }

    private void refreshChartsList(List<Chart> charts, CatalogWrapper cw) {
        charts.parallelStream()
                .forEach(
                        pkg -> {
                            try {
                                refreshPackage(cw, pkg);
                            } catch (CatalogLoaderException | IOException e) {
                                LOGGER.info("Exception occurred", e);
                            }
                        });
    }

    private void refreshPackage(CatalogWrapper cw, Pkg pkg)
            throws CatalogLoaderException, IOException {
        if (!(pkg instanceof Chart chart)) {
            throw new IllegalArgumentException("Package should be of type Chart");
        }

        LOGGER.info(
                "Refreshing package {} version {} in catalog {}",
                pkg.getName(),
                pkg.getVersion(),
                cw.getName());

        // One day we should take a look at the other URLs
        String chartUrl =
                chart.getUrls().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new CatalogLoaderException(
                                                "Package " + cw.getName() + " has no urls"));
        String absoluteUrl = chartUrl;
        if (!(chartUrl.startsWith("http") || chartUrl.startsWith("https"))) {
            absoluteUrl = StringUtils.applyRelativePath(cw.getLocation() + "/", chartUrl);
        }

        Resource resource = resourceLoader.getResource(absoluteUrl);

        try (InputStream inputStream = resource.getInputStream()) {
            extractDataFromTgz(inputStream, chart);
        } catch (IOException e) {
            throw new CatalogLoaderException(
                    "Exception occurred during loading resource: " + resource.getDescription(), e);
        }
    }

    public void extractDataFromTgz(InputStream in, Chart chart) throws IOException {
        String chartName = chart.getName();

        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            TarArchiveEntry entry;

            while ((entry = tarIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.endsWith(chartName + "/values.schema.json")
                        && !entryName.endsWith("charts/" + chartName + "/values.schema.json")) {
                    // TODO : mutualize objectmapper
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        tarIn.transferTo(baos);
                        chart.setConfig(mapper.readTree(baos.toString(UTF_8)));
                    }
                } else if (entryName.endsWith(chartName + "/values.yaml")
                        && !entryName.endsWith("charts/" + chartName + "/values.yaml")) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        tarIn.transferTo(baos);
                        chart.setDefaultValues(baos.toString());
                    }
                }
            }
        }
    }
}
