package fr.insee.onyxia.api.services.control.marathon;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.control.AdmissionControllerMarathon;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Enforce service id
 */
@Service
public class IDValidator implements AdmissionControllerMarathon {

    @Autowired
    IDSanitizer sanitizer;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    public boolean validateContract(Region region, Group group, App app, User user, UniversePackage pkg, Map<String, Object> configData,
                                    PublishContext context) {
        return true;
    }

}
