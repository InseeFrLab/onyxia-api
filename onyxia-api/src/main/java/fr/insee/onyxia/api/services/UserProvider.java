package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;

public interface UserProvider {

   /**
    * Parse the user from context.
    * User groups should match region group pattern.
    * @param region contextual region of the user
    * @return current user
    */
   public User getUser(Region region);
}
