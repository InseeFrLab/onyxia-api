package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OnyxiaLabelsEnforcer implements AdmissionController {

    @Override
    public boolean validateContract(App app, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context) {

        Map<String, String> onyxiaOptions = (Map<String,String>) configData.get("onyxia");
        app.addLabel("ONYXIA_NAME", pkg.getName());
        if (onyxiaOptions != null) {
            app.addLabel("ONYXIA_TITLE", onyxiaOptions.get("friendly_name"));
        }

        app.addLabel("ONYXIA_SUBTITLE", pkg.getName());
        app.addLabel("ONYXIA_SCM", pkg.getScm());
        app.addLabel("ONYXIA_DESCRIPTION", pkg.getDescription());
        app.addLabel("ONYXIA_STATUS", pkg.getStatus());
        if (!app.getLabels().containsKey("ONYXIA_URL") && app.getLabels().containsKey("HAPROXY_0_VHOST")) {
            if (app.getLabels().containsKey("HAPROXY_1_VHOST")) {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST") + ",https://"
                        + app.getLabels().get("HAPROXY_1_VHOST"));
            } else {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST"));
            }
        }
        app.addLabel("ONYXIA_LOGO", (String) ((Map<String, Object>) pkg.getResource().get("images")).get("icon-small"));


        return true;
    }

    @Override
    public Integer getPriority() {
        return -1000;
    }
}
