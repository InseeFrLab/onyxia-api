package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.region.Region;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final Catalogs catalogs;

    @Autowired
    public CatalogServiceImpl(Catalogs catalogs) {
        this.catalogs = catalogs;
    }

    @Override
    public Catalogs getCatalogs() {
        return catalogs;
    }

    @Override
    public Catalogs getCatalogs(Region region, User user) {
        return new Catalogs(
                catalogs.getCatalogs().stream()
                        .filter(catalog -> isCatalogVisibleToUser(user, catalog))
                        .toList());
    }

    private boolean isCatalogVisibleToUser(User user, CatalogWrapper catalog) {
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

    @Override
    public CatalogWrapper getCatalogById(String catalogId) {
        return catalogs.getCatalogById(catalogId);
    }

    @Override
    public Optional<Chart> getPackage(String catalogId, String packageName) {
        return catalogs.getCatalogById(catalogId).getCatalog().getPackageByName(packageName);
    }

    @Override
    public Optional<List<Chart>> getCharts(String catalogId, String chartName) {
        return Optional.ofNullable(
                catalogs.getCatalogById(catalogId).getCatalog().getEntries().get(chartName));
    }

    @Override
    public Optional<Chart> getChartByVersion(String catalogId, String chartName, String version) {
        List<Chart> charts =
                catalogs.getCatalogById(catalogId).getCatalog().getEntries().get(chartName);
        if (charts != null) {
            return charts.stream().filter(c -> c.getVersion().equalsIgnoreCase(version)).findAny();
        } else return Optional.empty();
    }
}
