package fr.insee.onyxia.api.services.control.marathon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlGenerator {

    @Value("${marathon.publish.domain}")
    private String baseDomain;

    public String generateUrl(String userId, String packageName, String generatedId, int portNumber) {
        String url = userId+"-"+packageName+"-"+generatedId+(portNumber != 0 ? "-"+portNumber : "")+"."+baseDomain;
        return url;
    }
}
