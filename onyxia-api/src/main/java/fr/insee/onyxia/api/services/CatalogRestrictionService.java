package fr.insee.onyxia.api.services;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.User;

public interface CatalogRestrictionService {

    boolean isCatalogVisibleToUser(User user, CatalogWrapper catalog);
}
