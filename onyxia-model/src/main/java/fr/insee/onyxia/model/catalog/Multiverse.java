package fr.insee.onyxia.model.catalog;

import java.util.ArrayList;
import java.util.List;

public class Multiverse {
  private List<UniverseWrapper> universes;


  public Multiverse() {

  }

  public UniverseWrapper getUniverseById(String id) {
    for (UniverseWrapper uc : universes) {
      if (uc.getId().equals(id)) {
        return uc;
      }
    }
    return null;
  }

  public List<UniverseWrapper> getUniverses() {
    return universes;
  }

  public void setUniverses(List<UniverseWrapper> universes) {
    this.universes = universes;
  }
}
