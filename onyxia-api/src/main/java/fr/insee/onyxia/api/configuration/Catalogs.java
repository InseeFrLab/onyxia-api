package fr.insee.onyxia.api.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "List of catalog wrapper")
public class Catalogs {
    private List<CatalogWrapper> catalogs = new ArrayList<>();

    public CatalogWrapper getCatalogById(final String id) {
        for (final CatalogWrapper cw : catalogs) {
            if (cw.getId().equals(id)) {
                return cw;
            }
        }
        return null;
    }

    /**
     * @return the catalogs
     */
    public List<CatalogWrapper> getCatalogs() {
        return catalogs;
    }

    /**
     * @param catalogs the catalogs to set
     */
    public void setCatalogs(List<CatalogWrapper> catalogs) {
        this.catalogs = catalogs;
    }
}
