package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.User;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatalogRestrictionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRestrictionService.class);

    public boolean isCatalogVisibleToUser(User user, CatalogWrapper catalog) {
        var catalogRestrictions = catalog.getRestrictions();
        if (catalogRestrictions.isEmpty()) {
            return true;
        }

        // As of now the only restrictions possible are related to users
        if (user == null) {
            return false;
        }

        return catalogRestrictions.stream()
                .allMatch(
                        restriction -> {
                            if (restriction.getUserAttribute() == null) return false;

                            String key = restriction.getUserAttribute().getKey();
                            Pattern regex = restriction.getUserAttribute().getMatches();

                            if (user.getAttributes().containsKey(key)) {

                                Object attribute = user.getAttributes().get(key);
                                if (attribute instanceof List<?> claims) {
                                    return claims.stream()
                                            .filter(String.class::isInstance)
                                            .map(String.class::cast)
                                            .anyMatch(
                                                    claimValue ->
                                                            regex.matcher(claimValue).matches());
                                } else if (attribute instanceof String claimValue) {
                                    return regex.matcher(claimValue).matches();
                                } else if (attribute instanceof Boolean claimValue) {
                                    return claimValue.toString().equalsIgnoreCase(regex.pattern());
                                }
                                LOGGER.info("claim {} was found, but type is not supported", key);
                            }

                            return false;
                        });
    }
}
