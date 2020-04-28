package fr.insee.onyxia.api.services.control.marathon;

import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Enforce service id
 */
@Service
public class IDEnforcer implements AdmissionController {

    @Autowired
    IDSanitizer sanitizer;

    @Value("${marathon.group.name}")
    private String MARATHON_GROUP_NAME;

    @Value("${cloudshell.catalogid}")
    private String cloudshellCatalogId;

    @Value("${cloudshell.packagename}")
    private String cloudshellPackageName;

    @Override
    public boolean validateContract(App app, User user, UniversePackage pkg, Map<String, Object> configData,
            PublishContext context) {
        if (cloudshellCatalogId.equals(context.getUniverseId()) && cloudshellPackageName.equals(pkg.getName())) {
            app.setId(MARATHON_GROUP_NAME + "/" + sanitizer.sanitize(user.getIdep()) + "/" + "cloudshell");
            return true;
        }

        app.setId(MARATHON_GROUP_NAME + "/" + sanitizer.sanitize(user.getIdep()) + "/"
                + sanitizer.sanitize(pkg.getName()) + "-" + context.getRandomizedId());
        return true;
    }

}
