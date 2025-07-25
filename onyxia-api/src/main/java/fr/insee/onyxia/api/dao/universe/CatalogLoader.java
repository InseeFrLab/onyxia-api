package fr.insee.onyxia.api.dao.universe;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.zafarkhaja.semver.Version;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.helm.Repository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CatalogLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogLoader.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper mapperHelm;

    private OkHttpClient httpClient;

    public CatalogLoader(
            ResourceLoader resourceLoader,
            @Qualifier("helm") ObjectMapper mapperHelm,
            @Qualifier("withCache") OkHttpClient httpClient) {
        this.resourceLoader = resourceLoader;
        this.mapperHelm = mapperHelm;
        this.httpClient = httpClient;
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

        try (InputStream stream =
                fetchResource(
                        cw.getLocation() + "/index.yaml",
                        cw.getUsername(),
                        cw.getPassword(),
                        true)) {
            Repository repository = mapperHelm.readValue(stream, Repository.class);

            repository.setEntries(
                    repository.getEntries().entrySet().stream()
                            .filter(
                                    // Remove explicitly excluded services
                                    entry ->
                                            cw.getExcludedCharts().stream()
                                                    .noneMatch(
                                                            excludedChart ->
                                                                    excludedChart.equalsIgnoreCase(
                                                                            entry.getKey())))
                            .filter(
                                    entry ->
                                            (CollectionUtils.isEmpty(cw.getIncludeKeywords())
                                                            || entry.getValue()
                                                                    .getFirst()
                                                                    .hasKeywords(
                                                                            cw
                                                                                    .getIncludeKeywords()))
                                                    && (CollectionUtils.isEmpty(
                                                                    cw.getIncludeAnnotations())
                                                            || entry.getValue()
                                                                    .getFirst()
                                                                    .hasAnnotations(
                                                                            cw
                                                                                    .getIncludeAnnotations())))
                            .filter(
                                    entry ->
                                            CollectionUtils.isEmpty(cw.getExcludeKeywords())
                                                    || !entry.getValue()
                                                            .getFirst()
                                                            .hasKeywords(cw.getExcludeKeywords()))
                            .filter(
                                    entry ->
                                            CollectionUtils.isEmpty(cw.getExcludeAnnotations())
                                                    || !entry.getValue()
                                                            .getFirst()
                                                            .hasAnnotations(
                                                                    cw.getExcludeAnnotations()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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

    private InputStream fetchResource(String url, String username, String password)
            throws IOException {
        return fetchResource(url, username, password, false);
    }

    private InputStream fetchResource(
            String url, String username, String password, boolean skipCache) throws IOException {
        if (url.startsWith("http")) {
            Request.Builder builder = new Request.Builder().url(url);
            if (username != null && password != null) {
                builder = builder.addHeader("Authorization", Credentials.basic(username, password));
            }
            if (skipCache) {
                builder.cacheControl(new CacheControl.Builder().noCache().build());
            }
            return httpClient.newCall(builder.build()).execute().body().byteStream();
        } else {
            return resourceLoader.getResource(url).getInputStream();
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

        try (InputStream inputStream =
                fetchResource(absoluteUrl, cw.getUsername(), cw.getPassword())) {
            extractDataFromTgz(inputStream, chart);
        } catch (IOException e) {
            throw new CatalogLoaderException(
                    "Exception occurred during loading resource: " + absoluteUrl, e);
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

    public JsonNode resolveInternalReferences(JsonNode schemaNode, ObjectMapper objectMapper)
            throws IOException {
        // Convert the main schema JSON node to JSONObject
        JSONObject schemaJson = new JSONObject(new JSONTokener(schemaNode.toString()));

        // Create a SchemaLoader
        SchemaLoader loader =
                SchemaLoader.builder()
                        .schemaJson(schemaJson)
                        .resolutionScope("file:///") // Setting a base URI for relative references
                        .build();

        // Load and expand the schema
        Schema schema = loader.load().build();

        // Convert the resolved schema back to JsonNode
        JSONObject resolvedSchemaJson = new JSONObject(schema.toString());
        return objectMapper.readTree(resolvedSchemaJson.toString());
    }

    public JsonNode resolveInternalReferences(JsonNode schemaNode) {
        return resolveInternalReferences(schemaNode, schemaNode);
    }

    private JsonNode resolveInternalReferences(JsonNode schemaNode, JsonNode rootNode) {
        if (schemaNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) schemaNode;
            Map<String, JsonNode> updates = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals("$ref") && field.getValue().isTextual()) {
                    String ref = field.getValue().asText();
                    if (ref.startsWith("#/definitions/")) {
                        String refName = ref.substring("#/definitions/".length());
                        JsonNode refNode = rootNode.at("/definitions/" + refName);
                        if (!refNode.isMissingNode()) {
                            JsonNode resolvedNode =
                                    resolveInternalReferences(refNode.deepCopy(), rootNode);
                            updates.putAll(convertToMap((ObjectNode) resolvedNode));
                            updates.put("$ref", null);
                        }
                    }
                } else {
                    updates.put(
                            field.getKey(), resolveInternalReferences(field.getValue(), rootNode));
                }
            }

            for (Map.Entry<String, JsonNode> update : updates.entrySet()) {
                if (update.getValue() == null) {
                    objectNode.remove(update.getKey());
                } else {
                    objectNode.set(update.getKey(), update.getValue());
                }
            }
        } else if (schemaNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) schemaNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, resolveInternalReferences(arrayNode.get(i), rootNode));
            }
        }
        return schemaNode;
    }

    private Map<String, JsonNode> convertToMap(ObjectNode objectNode) {
        Map<String, JsonNode> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            map.put(field.getKey(), field.getValue());
        }
        return map;
    }
}
