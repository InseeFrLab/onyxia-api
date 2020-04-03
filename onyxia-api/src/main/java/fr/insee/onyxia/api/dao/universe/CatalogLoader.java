package fr.insee.onyxia.api.dao.universe;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.catalog.Config.Config;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.helm.Repository;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

@Service
public class CatalogLoader {

    private final Logger logger = LoggerFactory.getLogger(CatalogRefresher.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("helm")
    private ObjectMapper mapperHelm;

    @Autowired
    private HelmRepoService helmService;

    public void updateCatalog(CatalogWrapper cw) {
        logger.info("updating catalog with id :" + cw.getId() + " and type " + cw.getType());
        switch (cw.getType()) {
            case Universe.TYPE_UNIVERSE:
                updateUniverse(cw);
                break;
            case Repository.TYPE_HELM:
                updateHelmRepository(cw);
                break;
        }
    }

    /**
     * TODO : move this universe specific logic somewhere else ?
     */
    private void updateUniverse(CatalogWrapper cw) {
        try {
            Reader reader = new InputStreamReader(resourceLoader.getResource(cw.getLocation()).getInputStream(),
                    "UTF-8");
            cw.setCatalog(mapper.readValue(reader, Universe.class));
            cw.setLastUpdateTime(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Property friendlyName = new Property();
        // friendlyName.setType("string");
        // friendlyName.setDescription("Nom d'affichage du service sur Onyxia");
        // friendlyName.setDefaut(pkg.getName());
        // friendlyName.setTitle("Un titre plus sympathique?");
        // Category onyxia = new Category();
        // Map<String, Property> map = new HashMap<String, Property>();
        // map.put("friendly_name", friendlyName);
        // onyxia.setProperties(map);
        // onyxia.setType("object");
        // onyxia.setDescription("Configure l'ensemble des metas-donnees pour Onyxia");
        // pkg.getProperties().getCategories().put("onyxia", onyxia);
    }

    /**
     * TODO : move this helm specific logic somewhere else ?
     */
    private void updateHelmRepository(CatalogWrapper cw) {
        try {
            updateHelmRepo(cw);
            Reader reader = new InputStreamReader(resourceLoader.getResource(cw.getLocation()).getInputStream(),
                    "UTF-8");
            Repository repository = mapperHelm.readValue(reader, Repository.class);
            repository.getPackages().parallelStream().forEach(pkg -> {
                try {
                    refreshPackage(pkg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            cw.setCatalog(repository);
            cw.setLastUpdateTime(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHelmRepo(CatalogWrapper cw) throws IOException, InterruptedException, TimeoutException {
        String location = cw.getLocation();
        Pattern pattern = Pattern.compile("(.*)/index.yaml");
        Matcher m =pattern.matcher(location);
        if (m.matches()){
            helmService.addHelmRepo(cw.getLocation(),cw.getName());
        }
    }

    private void refreshPackage(Package pkg) throws IOException {
        if (!(pkg instanceof Chart)) {
            throw new IllegalArgumentException("Package should be of type Chart");
        }

        Chart chart = (Chart) pkg;
        // TODO : support multiple urls
        InputStream inputStream = resourceLoader.getResource(chart.getUrls().stream().findFirst().get())
                .getInputStream();
        extractDataFromTgz(inputStream, chart);

        inputStream.close();
    }

    public void extractDataFromTgz(InputStream in, Chart chart) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        // HelmConfig config = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.getName().endsWith("values.schema.json")) {
                    logger.info("Found values.schema.json !");

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

            System.out.println("Untar completed successfully!");
        }

    }

}