package fr.insee.onyxia.api.services.control.marathon.security;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.region.Region;
import org.springframework.stereotype.Service;

@Service
public class PermissionsChecker {

    public void checkPermission(Region region, User user, String fullId) throws IllegalAccessException {
        if (!fullId.startsWith("/"+region.getServices().getNamespacePrefix()+"/"+user.getIdep())) {
            throw new IllegalAccessException("User "+user.getIdep()+" can not access "+fullId);
        }
    }
}
