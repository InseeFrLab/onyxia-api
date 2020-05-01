package fr.insee.onyxia.api.services.control.commons;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class UrlGenerator {

    public String generateUrl(String userId, String id, String generatedId, String prefix,
            String baseDomain) {
        String url = userId + "-" + id + "-" + generatedId + (StringUtils.isNotBlank(prefix) ? "-"+prefix : "")  + "."
                + baseDomain;
        return url;
    }
}
