package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;

import java.util.Map;

public interface AdmissionController {

    /**
     * Validate that the contract is ok to deploy
     * Implementations are free to modify the contract
     * @param app : the app to deploy
     * @param user : user info
     * @param pkg : the package contract
     * @param configData : configuration data
     * @return whether the contract is ok to deploy
     */
    public boolean validateContract(App app, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context);
}
