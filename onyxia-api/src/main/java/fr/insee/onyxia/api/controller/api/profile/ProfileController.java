package fr.insee.onyxia.api.controller.api.profile;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.onyxia.api.services.JsonSchemaProfileRegistryService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "Info on profile configuration feature")
@RequestMapping("/profile")
@RestController
@SecurityRequirement(name = "auth")
public class ProfileController {

    private final UserProvider userProvider;

    private final JsonSchemaProfileRegistryService jsonSchemaProfileRegistryService;

    @Autowired
    public ProfileController(
            UserProvider userProvider,
            JsonSchemaProfileRegistryService jsonSchemaProfileRegistryService) {
        this.userProvider = userProvider;
        this.jsonSchemaProfileRegistryService = jsonSchemaProfileRegistryService;
    }

    @GetMapping("schema")
    public JsonNode profileSchema(@Parameter(hidden = true) Region region) {
        User user = userProvider.getUser(region);
        return jsonSchemaProfileRegistryService.getProfileSchema();
    }
}
