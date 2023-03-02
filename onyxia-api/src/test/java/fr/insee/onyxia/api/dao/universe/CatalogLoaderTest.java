package fr.insee.onyxia.api.dao.universe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.CustomObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CatalogLoader.class, CustomObjectMapper.class})
public class CatalogLoaderTest {

    @Autowired CatalogLoader catalogLoader;

    @DisplayName(
            "Given a helm catalog wrapper with local charts and excluded charts, "
                    + "when we update the catalog, "
                    + "then the excluded chart is not set")
    @Test
    public void excludeChartTest() {
        CatalogWrapper cw = new CatalogWrapper();
        cw.setType("helm");
        cw.setLocation("classpath:/catalog-loader-test");
        cw.setExcludedCharts(List.of("excludemetoo", "excludeme"));
        catalogLoader.updateCatalog(cw);
        assertThat(
                "cw has the not excluded entries",
                cw.getCatalog().getEntries().get("keepme").size(),
                is(2));
        assertThat(
                "cw has the not excluded package",
                cw.getCatalog().getPackages().stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase("keepme")));
        assertThat(
                "cw does not have the excluded entries",
                !cw.getCatalog().getEntries().containsKey("excludeme"));
        assertThat(
                "cw does not have the excluded packages",
                cw.getCatalog().getPackages().stream()
                        .noneMatch(p -> p.getName().equalsIgnoreCase("excludeme")));
    }
}
