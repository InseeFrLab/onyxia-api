package fr.insee.onyxia.api.services.control.marathon;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.model.v2.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Enforce service id
 */
@Service
public class IDEnforcer implements AdmissionController {

    @Autowired
    IDSanitizer sanitizer;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    public boolean validateContract(Region region, App app, User user, UniversePackage pkg, Map<String, Object> configData,
            PublishContext context) {
        Region.CloudshellConfiguration cloudshellConfiguration = region.getCloudshellConfiguration();
        if (cloudshellConfiguration != null && cloudshellConfiguration.getCatalogId().equals(context.getUniverseId()) && cloudshellConfiguration.getPackageName().equals(pkg.getName())) {
            app.setId(region.getNamespacePrefix() + "/" + sanitizer.sanitize(user.getIdep()) + "/" + "cloudshell");
            return true;
        }

        app.setId(region.getNamespacePrefix() + "/" + sanitizer.sanitize(user.getIdep()) + "/"
                + sanitizer.sanitize(pkg.getName()) + "-" + context.getRandomizedId());
        return true;
    }

}
