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

    @Autowired private Catalogs catalogs;

    @GetMapping("/healthcheck")
    public ResponseEntity healthcheck() {

        List<CatalogWrapper> uninitializedCatalogs =
                catalogs.getCatalogs().stream()
                        .filter(catalogWrapper -> catalogWrapper.getLastUpdateTime() == 0)
                        .toList();
        if (uninitializedCatalogs.size() != 0) {
            LOGGER.info(
                    "Uninitialized catalogs : {}",
                    uninitializedCatalogs.stream()
                            .map(CatalogWrapper::getId)
                            .collect(Collectors.joining(", ")));
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.status(200).build();
    }
}
