package fr.insee.onyxia.api.services.control.commons;

import org.springframework.stereotype.Service;

@Service
public class UrlGenerator {

    public String generateUrl(String userId, String packageName, String generatedId, int portNumber,
            String baseDomain) {
        String url = userId + "-" + packageName + "-" + generatedId + (portNumber != 0 ? "-" + portNumber : "") + "."
                + baseDomain;
        return url;
    }
}
