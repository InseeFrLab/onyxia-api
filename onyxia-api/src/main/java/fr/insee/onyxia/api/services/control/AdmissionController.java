package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;

public interface AdmissionController {

    /**
     * Validate that the contract is ok to deploy
     * Implementations are free to modify the contract
     * @param user
     * @param pkg : the package to deploy
     * @param data : configuration data
     * @return whether the contract is ok to deploy
     */
    public boolean validateContract(User user, UniversePackage pkg, Object data);
}
