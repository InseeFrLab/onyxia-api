package fr.insee.onyxia.api.services.control.marathon;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.control.AdmissionControllerMarathon;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class URLEnforcer implements AdmissionControllerMarathon {

    private static Pattern PATTERN_HAPROXY_VHOST = Pattern.compile("HAPROXY_([0-9]*)_VHOST");

    @Autowired
    private UrlGenerator urlGenerator;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    public boolean validateContract(Region region, Group group, App app, User user, UniversePackage pkg, Map<String, Object> configData,
                                    PublishContext context) {
        return true;
    }

    @Override
    public Integer getPriority() {
        return Integer.MAX_VALUE;
    }
}
