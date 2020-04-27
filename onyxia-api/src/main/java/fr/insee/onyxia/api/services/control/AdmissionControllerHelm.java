package fr.insee.onyxia.api.services.control;

import java.util.Map;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;

public interface AdmissionControllerHelm {
    /**
     * Validate that the contract is ok to deploy Implementations are free to modify
     * the contract
     * 
     * @param app        : the app to deploy
     * @param user       : user info
     * @param pkg        : the package contract
     * @param configData : configuration data
     * @return whether the contract is ok to deploy
     */
    public boolean validateContract(Package pkg, Map<String, Object> values, User user);

    /**
     * Returns the priority. Higher priority will be applied first. Default is 0,
     * can be negative.
     * 
     * @return
     */
    public default Integer getPriority() {
        return 0;
    }
}