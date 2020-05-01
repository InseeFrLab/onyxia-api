package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;

import java.util.Map;

public interface AdmissionControllerMarathon {

    /**
     * Validate that the contract is ok to deploy Implementations are free to modify
     * the contract
     *
     * @param region : the region to deploy to
     * @param enclosingGroup : The enclosing group, null otherwise
     * @param app        : the app to deploy
     * @param user       : user info
     * @param pkg        : the package contract
     * @param configData : configuration data
     * @return whether the contract is ok to deploy
     */
    public boolean validateContract(Region region, Group enclosingGroup, App app, User user, UniversePackage pkg, Map<String, Object> configData,
                                    PublishContext context);

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
