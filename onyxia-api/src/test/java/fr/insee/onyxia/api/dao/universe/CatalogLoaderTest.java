package fr.insee.onyxia.api.dao.universe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.CustomObjectMapper;
import fr.insee.onyxia.api.services.JsonSchemaRegistryService;
import fr.insee.onyxia.api.services.JsonSchemaResolutionService;
import fr.insee.onyxia.api.util.TestUtils;
import fr.insee.onyxia.model.helm.Chart;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
            CatalogLoader.class,
            CustomObjectMapper.class,
            JsonSchemaResolutionService.class,
            JsonSchemaRegistryService.class
        })
public class CatalogLoaderTest {

    @Autowired CatalogLoader catalogLoader;

    @Autowired ResourceLoader resourceLoader;

    @DisplayName(
            "Given a helm catalog wrapper with local charts and excluded charts, "
                    + "when we update the catalog, "
                    + "then the excluded chart is not set")
    @Test
    void excludeChartTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setExcludedCharts(List.of("excludemetoo", "excludeme"));
        cw.setMultipleServicesMode(CatalogWrapper.MultipleServicesMode.ALL);
        catalogLoader.updateCatalog(cw);
        assertThat(
                "cw has the not excluded entries",
                cw.getCatalog().getEntries().get("keepme").size(),
                is(3));
        assertThat(
                "cw does not have the excluded entries",
                !cw.getCatalog().getEntries().containsKey("excludeme"));
    }

    @Test
    void multipleVersionsAllTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setMultipleServicesMode(CatalogWrapper.MultipleServicesMode.ALL);
        catalogLoader.updateCatalog(cw);
        assertEquals(3, cw.getCatalog().getEntries().get("keepme").size());
    }

    @Test
    void multipleVersionsLatestTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setMultipleServicesMode(CatalogWrapper.MultipleServicesMode.LATEST);
        catalogLoader.updateCatalog(cw);
        assertEquals(1, cw.getCatalog().getEntries().get("keepme").size());
        assertEquals("2.5.1", cw.getCatalog().getEntries().get("keepme").getFirst().getVersion());
        assertEquals(1, cw.getCatalog().getEntries().get("excludeme").size());
        assertEquals(
                "2.4.1", cw.getCatalog().getEntries().get("excludeme").getFirst().getVersion());
    }

    @Test
    void multipleVersionsSkipPatchesTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setMultipleServicesMode(CatalogWrapper.MultipleServicesMode.SKIP_PATCHES);
        catalogLoader.updateCatalog(cw);
        assertEquals(2, cw.getCatalog().getEntries().get("keepme").size());
        assertEquals("2.5.1", cw.getCatalog().getEntries().get("keepme").get(0).getVersion());
        assertEquals("2.4.1", cw.getCatalog().getEntries().get("keepme").get(1).getVersion());
        assertEquals(1, cw.getCatalog().getEntries().get("excludeme").size());
        assertEquals("2.4.1", cw.getCatalog().getEntries().get("excludeme").get(0).getVersion());
    }

    @Test
    void multipleVersionsMaxNumberTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setMultipleServicesMode(CatalogWrapper.MultipleServicesMode.MAX_NUMBER);
        cw.setMaxNumberOfVersions(2);
        catalogLoader.updateCatalog(cw);
        assertEquals(2, cw.getCatalog().getEntries().get("keepme").size());
        assertEquals("2.5.1", cw.getCatalog().getEntries().get("keepme").get(0).getVersion());
        assertEquals("2.4.1", cw.getCatalog().getEntries().get("keepme").get(1).getVersion());
        assertEquals(2, cw.getCatalog().getEntries().get("excludeme").size());
        assertEquals("2.4.1", cw.getCatalog().getEntries().get("excludeme").get(0).getVersion());
        assertEquals("2.4.0", cw.getCatalog().getEntries().get("excludeme").get(1).getVersion());
    }

    @Test
    void loadMaintainers() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setExcludedCharts(List.of("excludemetoo", "excludeme"));
        catalogLoader.updateCatalog(cw);
        List<List<Chart.Maintainer>> maintainers =
                cw.getCatalog().getEntries().entrySet().stream()
                        .map(entry -> entry.getValue().stream().findFirst().get().getMaintainers())
                        .collect(Collectors.toList());
        assertThat(
                "Maintainers have been loaded",
                maintainers.stream()
                        .filter(l -> !CollectionUtils.isEmpty(l))
                        .anyMatch(
                                l ->
                                        l.get(0).getName().equals("test")
                                                && l.get(0).getEmail().equals("test@example.com")));
    }

    @DisplayName(
            "Given a helm catalog wrapper with local charts and excluded charts, "
                    + "when we update the catalog, "
                    + "then failed packages should be logged")
    @Test
    void packageOnClassPathNotFound() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        catalogLoader.updateCatalog(cw);

        String stdErrLogs = TestUtils.tapSystemOut(() -> catalogLoader.updateCatalog(cw));

        assertThat(
                stdErrLogs,
                containsString(
                        "fr.insee.onyxia.api.dao.universe.CatalogLoaderException: "
                                + "Exception occurred during loading resource: class path resource "
                                + "[catalog-loader-test/keepeme1.gz]"));
    }

    @ParameterizedTest
    @MethodSource("includeKeywords")
    @MethodSource("excludeKeywords")
    void filterServicesTest(List<String> includeKeywords, List<String> excludeKeywords, Set<String> expectedServices) {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test-with-keywords");
        cw.setIncludeKeywords(includeKeywords);
        cw.setExcludeKeywords(excludeKeywords);
        catalogLoader.updateCatalog(cw);
        assertEquals(expectedServices, cw.getCatalog().getEntries().keySet());
    }

    private static Stream<Arguments> includeKeywords() {
        return Stream.of(
                arguments(List.of("CD"), null, Set.of("keepme")),
                arguments(List.of("CD", "Experimental"), null, Set.of("keepme", "excludeme")),
                arguments(List.of(), null, Set.of("keepme", "excludeme")),
                arguments(null, null, Set.of("keepme", "excludeme")),
                arguments(List.of("no one knows"), null, Set.of()));
    }

    private static Stream<Arguments> excludeKeywords() {
        return Stream.of(
                arguments(null, List.of("Experimental"), Set.of("keepme")),
                arguments(List.of("CD"), List.of("Experimental"), Set.of("keepme")),
                arguments(List.of("Experimental"), List.of("Experimental"), Set.of()),
                arguments(List.of("CD"), List.of("CD", "Experimental"), Set.of()),
                arguments(List.of(), List.of(), Set.of("keepme", "excludeme")),
                arguments(null, List.of("no one knows"), Set.of("keepme", "excludeme"))
        );
    }
}
