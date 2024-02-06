package fr.insee.onyxia.api.controller.pub;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Public")
@RequestMapping("/public")
public class HealthcheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthcheckController.class);

    private Catalogs catalogs;

    @Autowired
    public HealthcheckController(Catalogs catalogs) {
        this.catalogs = catalogs;
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<Void> healthcheck() {
        List<CatalogWrapper> uninitializedCatalogs =
                catalogs.getCatalogs().stream()
                        .filter(catalogWrapper -> catalogWrapper.getLastUpdateTime() == 0)
                        .toList();
        if (!uninitializedCatalogs.isEmpty()) {

            String notInitializedCatalogs =
                    uninitializedCatalogs.stream()
                            .map(CatalogWrapper::getId)
                            .collect(Collectors.joining(", "));
            LOGGER.info("Uninitialized catalogs : {}", notInitializedCatalogs);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.status(200).build();
    }
}
