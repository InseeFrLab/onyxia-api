package fr.insee.onyxia.api.controller.api.admin;

import fr.insee.onyxia.api.dao.universe.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Restricted API endpoints")
@RequestMapping("/admin")
@RestController
@SecurityRequirement(name = "auth")
@ConditionalOnExpression("'${admin-endpoints-enabled}' == 'true'")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private final CatalogRefresher catalogRefresher;

    @PostConstruct
    private void postConstruct() {
        LOGGER.warn("RUNNING IN ADMIN MODE!!!");
    }

    @Autowired
    public AdminController(CatalogRefresher catalogRefresher) {
        this.catalogRefresher = catalogRefresher;
    }

    @GetMapping("/refreshCatalogs")
    public void refreshCatalogs() {
        catalogRefresher.run();
    }
}
