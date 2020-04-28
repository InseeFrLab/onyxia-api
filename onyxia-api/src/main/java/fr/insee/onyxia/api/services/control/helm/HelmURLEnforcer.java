package fr.insee.onyxia.api.services.control.helm;

import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.helm.Chart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class HelmURLEnforcer implements AdmissionControllerHelm {

    @Value("${kubernetes.publish.domain}")
    private String baseDomain;

    @Autowired
    private UrlGenerator urlGenerator;

    @Override
    public boolean validateContract(Package pkg, Map<String, Object> values, User user) {
        Chart chart = (Chart) pkg;
        Map<String, Property> props = chart.getConfig().getProperties().getProperties();
        if (props.containsKey("ingress") && props.get("ingress").getProperties().containsKey("hostname")) {
            Map<String, String> newIngressConfig = new HashMap<>();
            if (values.containsKey("ingress")) {
                newIngressConfig = (Map<String,String>) values.get("ingress");
            }
            newIngressConfig.put("hostname",getUrl(user,pkg));
            values.put("ingress",newIngressConfig);
        }
        return true;
    }

    private String getUrl(User user, Package pkg) {
        return urlGenerator.generateUrl(user.getIdep(), pkg.getName(),
                String.valueOf(UUID.randomUUID().getLeastSignificantBits()), 0, baseDomain);
    }

}