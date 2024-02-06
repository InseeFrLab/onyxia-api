package fr.insee.onyxia.api.services.impl;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.region.Region;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final Catalogs catalogs;
    private final CatalogRestrictionService catalogRestrictionService;

    @Autowired
    public CatalogServiceImpl(
            Catalogs catalogs, CatalogRestrictionService catalogRestrictionService) {
        this.catalogs = catalogs;
        this.catalogRestrictionService = catalogRestrictionService;
    }

    @Override
    public Catalogs getCatalogs() {
        return catalogs;
    }

    @Override
    public Catalogs getCatalogs(Region region, User user) {
        return new Catalogs(getAuthorizedCatalogsStream(user).toList());
    }

    @Override
    public Optional<CatalogWrapper> getCatalogById(String catalogId, User user) {
        return getAuthorizedCatalogsStream(user)
                .filter(catalog -> catalog.getId().equals(catalogId))
                .findFirst();
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

    private Stream<CatalogWrapper> getAuthorizedCatalogsStream(User user) {
        return catalogs.getCatalogs().stream()
                .filter(catalog -> catalogRestrictionService.isCatalogVisibleToUser(user, catalog));
    }
}
