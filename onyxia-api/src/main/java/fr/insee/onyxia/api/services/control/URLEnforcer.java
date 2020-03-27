package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class URLEnforcer implements AdmissionController {

    private static Pattern PATTERN_HAPROXY_VHOST = Pattern.compile("HAPROXY_([0-9]*)_VHOST");

    @Value("${marathon.publish.domain}")
    private String baseDomain;

    @Override
    public boolean validateContract(App app, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context) {
        List<Integer> openedPortsIds = app.getLabels().entrySet()
                .stream()
                .filter(entry -> PATTERN_HAPROXY_VHOST.matcher(entry.getKey()).matches())
                .map(entry -> {
                    Matcher matcher = PATTERN_HAPROXY_VHOST.matcher(entry.getKey());
                    matcher.find();
                    return Integer.parseInt(matcher.group(1));
                })
                .collect(Collectors.toList());
        openedPortsIds.stream().forEach(portId -> {
            app.addLabel("HAPROXY_"+portId+"_VHOST",getUrl(portId,user,pkg,configData,context));
            app.addLabel("HAPROXY_"+portId+"_ENABLED","true");
        });
        return true;
    }

    private String getUrl(int portNumber, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context) {
        String url = user.getIdep()+"-"+pkg.getName()+"-"+context.getRandomizedId()+(portNumber != 0 ? "-"+portNumber : "")+"."+baseDomain;
        return url;
    }
}
