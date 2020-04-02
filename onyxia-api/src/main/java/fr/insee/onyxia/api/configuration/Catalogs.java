package fr.insee.onyxia.api.configuration;

import java.util.ArrayList;
import java.util.List;

public class Catalogs {
  private List<CatalogWrapper> catalogs = new ArrayList<>();

  public Catalogs() {

  }

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
  public void setCatalogs(final List<CatalogWrapper> catalogs) {
    this.catalogs = catalogs;
  }
}
