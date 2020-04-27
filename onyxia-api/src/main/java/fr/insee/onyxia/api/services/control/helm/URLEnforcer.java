package fr.insee.onyxia.api.services.control.helm;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;

public class URLEnforcer implements AdmissionControllerHelm {

    @Value("${kubernetes.publish.domain}")
    private String baseDomain;

    @Autowired
    private UrlGenerator urlGenerator;

    @Override
    public boolean validateContract(Package pkg, Map<String, Object> values, User user) {
        if (values.containsKey("ingress.hostname")) {
            values.put("ingress.hostname", getUrl(user, pkg));
            return true;
        }
        return false;
    }

    private String getUrl(User user, Package pkg) {
        return urlGenerator.generateUrl(user.getIdep(), pkg.getName(),
                String.valueOf(UUID.randomUUID().getLeastSignificantBits()), 0, baseDomain);
    }

}