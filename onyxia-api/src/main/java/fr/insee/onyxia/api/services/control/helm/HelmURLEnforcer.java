package fr.insee.onyxia.api.services.control.helm;

import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.control.AdmissionControllerHelm;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HelmURLEnforcer implements AdmissionControllerHelm {

    @Autowired
    private UrlGenerator urlGenerator;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Override
    public boolean validateContract(Region region, Package pkg, Map<String, Object> values, User user, PublishContext context) {
        Chart chart = (Chart) pkg;
        Map<String, Property> props = chart.getConfig().getProperties().getProperties();
        if (props.containsKey("ingress")) {
            Map<String, Object> newIngressConfig = new HashMap<>();
            if (values.containsKey("ingress")) {
                newIngressConfig = (Map<String,Object>) values.get("ingress");
            }
            newIngressConfig.put("hostname",getUrl(region.getPublishDomain(), user,pkg, context));
            newIngressConfig.put("enabled", true);
            values.put("ingress",newIngressConfig);
        }
        return true;
    }

    private String getUrl(String publishDomain, User user, Package pkg, PublishContext context) {
        return urlGenerator.generateUrl(user.getIdep(), pkg.getName(),
                context.getGlobalContext().getRandomizedId(), "", publishDomain);
    }

}