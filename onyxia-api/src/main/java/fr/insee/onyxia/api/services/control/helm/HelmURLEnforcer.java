package fr.insee.onyxia.api.services.control.helm;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HelmURLEnforcer implements AdmissionControllerHelm {

    @Autowired
    private UrlGenerator urlGenerator;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    public boolean validateContract(Region region, Pkg pkg, Map<String, Object> values, User user, PublishContext context) {
        // STUB
        return true;
    }

}