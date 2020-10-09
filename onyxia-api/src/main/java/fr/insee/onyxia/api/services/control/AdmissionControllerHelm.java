package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.region.Region;

import java.util.Map;

public interface AdmissionControllerHelm {
    /**
     * Validate that the contract is ok to deploy Implementations are free to modify
     * the contract
     *
     * @return whether the contract is ok to deploy
     */
    public boolean validateContract(Region region, Pkg pkg, Map<String, Object> values, User user, PublishContext context);

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