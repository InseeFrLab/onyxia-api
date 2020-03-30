package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enforce service id
 */
@Service
public class IDEnforcer implements AdmissionController {

    @Value("${marathon.group.name}")
    private String MARATHON_GROUP_NAME;

    @Override
    public boolean validateContract(App app, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context) {
        if ("internal".equals(context.getUniverseId()) && "shelly".equals(pkg.getName())) {
            app.setId(MARATHON_GROUP_NAME + "/" + user.getIdep() + "/" + "cloudshell");
            return true;
        }

        app.setId(MARATHON_GROUP_NAME + "/" + sanitize(user.getIdep()) + "/" + sanitize(pkg.getName()) + "-" + context.getRandomizedId());
        return true;
    }

    public String sanitize(String text){
        text = text.replaceAll("[^a-zA-Z0-9]", "");
        return text.toLowerCase();
    }
}
